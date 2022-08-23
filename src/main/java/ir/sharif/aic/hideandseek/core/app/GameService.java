package ir.sharif.aic.hideandseek.core.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.commands.ChatCommand;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.MoveCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import ir.sharif.aic.hideandseek.core.events.*;
import ir.sharif.aic.hideandseek.core.exceptions.PreconditionException;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.core.watchers.EventLogger;
import ir.sharif.aic.hideandseek.core.watchers.NextTurnWatcher;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import ir.sharif.aic.hideandseek.lib.channel.PubSubChannel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Deaths, Result and Status, Visibility */
@Service
public class GameService {
  private final GameConfig gameConfig;
  private final Channel<GameEvent> eventChannel;
  @Getter @Setter
  private Turn turn;
  @Getter private GameStatus status;
  private GameResult result;
  private final List<Chat> chatBox;

  public GameService(GameConfig gameConfig, ObjectMapper objectMapper) {
    this.gameConfig = gameConfig;
    this.eventChannel = new PubSubChannel<>();
    this.status = GameStatus.PENDING;
    this.result = GameResult.UNKNOWN;
    this.turn = new Turn(1, TurnType.THIEF_TURN);
    this.chatBox = new ArrayList<>();
    this.eventChannel.addWatcher(new NextTurnWatcher(this.eventChannel, gameConfig, this));
    this.eventChannel.addWatcher(new EventLogger(objectMapper));
  }

  public synchronized void handle(DeclareReadinessCommand cmd) {
    cmd.validate();
    var agent = this.gameConfig.findAgentByToken(cmd.getToken());
    if(agent.isReady())
      throw new PreconditionException("you have already joined");
    if(!gameConfig.getAllNodes().stream().anyMatch(e -> e.getId() == cmd.getStartNodeId()))
      throw new PreconditionException("Node with id:  " + cmd.getStartNodeId() + " doesn't exist!");
    if(!status.equals(GameStatus.PENDING))
      throw new PreconditionException("You can't join the game cause the game status is not PENDING any more!");
    agent.apply(cmd, this.eventChannel);

    if (this.gameConfig.everyAgentIsReady()) {
      changeGameStatusTo(GameStatus.ONGOING);
    }
  }

  public synchronized void handle(WatchCommand cmd) {
    cmd.validate();
    assertThatGameIsNotFinished("you can't watch cause game is finish.");
    this.gameConfig.assertAgentExistsWithToken(cmd.getToken());

    // send initial view
    var view = this.getView(cmd.getToken());
    cmd.getWatcher().getObserver().onNext(view);

    // add watcher to stream the following events
    this.eventChannel.addWatcher(cmd.getWatcher());
  }

  public synchronized void handle(MoveCommand cmd) {
    cmd.validate();
    var agent = this.gameConfig.findAgentByToken(cmd.getToken());

    if (agent.cannotDoActionOnTurn(this.turn.getTurnType())) {
      throw new PreconditionException("it's not your turn yet.");
    }

    assertThatGameIsNotFinished("you can't move because the game is finished.");

    if (!agent.isReady()) {
      throw new PreconditionException("you have not declared your readiness yet.");
    }

    if (!this.status.equals(GameStatus.ONGOING)) {
      throw new PreconditionException(
          "game state is %s , you can only move on %s state."
              .formatted(this.status.toString(), GameStatus.ONGOING.toString()));
    }

    if (agent.hasMovedThisTurn()) {
      throw new PreconditionException("you can't move anymore.");
    }

    if (agent.isDead()) {
      throw new PreconditionException("you are not alive to do any action.");
    }

    var src = agent.getNodeId();
    var dst = cmd.getToNodeId();

    if (src == dst) {
      agent.stayInPlace(eventChannel);
    } else {
      var path = this.gameConfig.findPath(src, dst);
      agent.moveAlong(path, this.eventChannel);
    }

//    if (this.gameConfig.everyAgentHasMovedThisTurn(
//        this.turn.getTurnType().equals(TurnType.THIEF_TURN) ? AgentType.THIEF : AgentType.POLICE)) {
//      this.gameConfig.getAllAgents().forEach(Agent::onTurnChange);
//      this.turn = this.turn.next();
//      this.eventChannel.push(
//              new GameTurnChangedEvent(this.turn.getTurnType(), getCurrentTurnNumber()));
//    }
  }

  public synchronized void handle(ChatCommand cmd) {
    cmd.validate();
    var agent = this.gameConfig.findAgentByToken(cmd.getToken());

    if (agent.cannotDoActionOnTurn(this.turn.getTurnType())) {
      throw new PreconditionException("it's not your turn yet.");
    }

    assertThatGameIsNotFinished("you can't send a message because the game is finished!");

    if (!agent.isReady()) {
      throw new PreconditionException("you have not declared your readiness yet.");
    }

    if (!this.status.equals(GameStatus.ONGOING)) {
      throw new PreconditionException(
          "game state is %s, you can only move on %s state."
              .formatted(this.status.toString(), GameStatus.ONGOING.toString()));
    }

    if (agent.hasSentMessageThisTurn()) {
      throw new PreconditionException("you have already sent a message this turn.");
    }

    if (agent.isDead()) {
      throw new PreconditionException("you are not alive to do any action.");
    }

    agent.sendMessage(cmd, this.chatBox, this.gameConfig.getChatSettings(), this.eventChannel);
  }

  public synchronized void arrestThieves(Node node, Team team) {
    if (this.gameConfig.checkTeamPoliceInNode(team, node)) {
      var thieves = this.gameConfig.findAllThievesByTeamAndNode(team.otherTeam(), node);
      thieves.forEach(agent -> agent.arrest(this.getCurrentTurnNumber()));
      thieves.forEach(
          thief -> eventChannel.push(new PoliceCaughtThiefEvent(node.getId(), thief.getId())));
    }
  }

  public synchronized void changeGameResultTo(GameResult gameResult) {
    if (!this.result.equals(GameResult.UNKNOWN)) return;
    this.result = gameResult;
    this.eventChannel.push(new GameResultChangedEvent(gameResult));
    this.changeGameStatusTo(GameStatus.FINISHED);
    this.logGameResult(gameResult);
    // TODO: this.eventChannel.close();
  }

  private void logGameResult(GameResult gameResult) {
    int winner = -1;
    switch (gameResult) {
      case UNKNOWN -> {
        return;
      }
      case FIRST_WINS -> winner = 0;
      case SECOND_WINS -> winner = 1;
    }

    String jsonString = "{\"stats\":{\"winner\":"+winner+"}}";
    try {
      var writer = new BufferedWriter(new FileWriter("logs/winner.log"));
      writer.write(jsonString);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized void changeGameStatusTo(GameStatus gameStatus) {
    var previousStatus = this.status;
    this.status = gameStatus;
    this.eventChannel.push(new GameStatusChangedEvent(previousStatus, gameStatus));
  }

  public HideAndSeek.GameView getView(String fromToken) {
    var viewerAgent = this.gameConfig.findAgentByToken(fromToken);
    var visibleAgents =
        this.gameConfig.findVisibleAgentsByViewerAndTurn(viewerAgent, this.turn).stream()
            .map(Agent::toProto)
            .toList();
    var visibleChatBox =
        this.chatBox.stream()
            .filter(
                chat ->
                    chat.isFromTeam(viewerAgent.getTeam())
                        && chat.isFromType(viewerAgent.getType()))
            .map(Chat::toProto)
            .toList();

    var upperChatBound =
        Math.min(this.gameConfig.getChatSettings().getChatBoxMaxSize(), visibleChatBox.size());
    visibleChatBox = visibleChatBox.subList(0, upperChatBound);

    return HideAndSeek.GameView.newBuilder()
        .setStatus(this.status.toProto())
        .setViewer(viewerAgent.toProto())
        .setResult(this.result.toProto())
        .setConfig(this.gameConfig.toProto())
        .setTurn(this.turn.toProto())
        .setBalance(viewerAgent.getBalance())
        .addAllVisibleAgents(visibleAgents)
        .addAllChatBox(visibleChatBox)
        .build();
  }

  public boolean isAllTurnsFinished() {
    return this.gameConfig.getMaxTurns() <= this.getCurrentTurnNumber();
  }

  public int getCurrentTurnNumber() {
    return this.turn.getTurnNumber();
  }

  private synchronized void assertThatGameIsNotFinished(String msg) {
    if (this.status.equals(GameStatus.FINISHED) && !this.result.equals(GameResult.UNKNOWN)) {
      throw new PreconditionException(msg);
    }
  }
}
