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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/** Deaths, Result and Status, Visibility */
@Service
public class GameService {
  private final GameConfig gameConfig;
  private final Channel<GameEvent> eventChannel;
  @Getter private Turn turn;
  private GameStatus status;
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

  public void handle(DeclareReadinessCommand cmd) {
    cmd.validate();
    var agent = this.gameConfig.findAgentByToken(cmd.getToken());
    agent.apply(cmd, this.eventChannel);

    if (this.gameConfig.everyAgentIsReady()) {
      changeGameStatusTo(GameStatus.ONGOING);
    }
  }

  public void handle(WatchCommand cmd) {
    cmd.validate();
    assertThatGameIsNotFinished("you can't watch cause game is finish.");
    this.gameConfig.assertAgentExistsWithToken(cmd.getToken());

    // send initial view
    var view = this.getView(cmd.getToken());
    cmd.getWatcher().getObserver().onNext(view);

    // add watcher to stream the following events
    this.eventChannel.addWatcher(cmd.getWatcher());
  }

  public void handle(MoveCommand cmd) {
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

    if (this.gameConfig.everyAgentHasMovedThisTurn(
        this.turn.getTurnType().equals(TurnType.THIEF_TURN) ? AgentType.THIEF : AgentType.POLICE)) {
      this.gameConfig.getAllAgents().forEach(Agent::onTurnChange);
      this.turn = this.turn.next();
      this.eventChannel.push(
          new GameTurnChangedEvent(this.turn.getTurnType(), getCurrentTurnNumber()));
    }
  }

  public void handle(ChatCommand cmd) {
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

  public void arrestThieves(Node node, Team team) {
    if (this.gameConfig.checkTeamPoliceInNode(team, node)) {
      var thieves = this.gameConfig.findAllThievesByTeamAndNode(team.otherTeam(), node);
      thieves.forEach(Agent::arrest);
      thieves.forEach(
          thief -> eventChannel.push(new PoliceCaughtThiefEvent(node.getId(), thief.getId())));
    }
  }

  public void changeGameResultTo(GameResult gameResult) {
    if (!this.result.equals(GameResult.UNKNOWN)) return;
    this.result = gameResult;
    this.changeGameStatusTo(GameStatus.FINISHED);

    this.eventChannel.push(new GameResultChangedEvent(gameResult));
    // TODO: this.eventChannel.close();
  }

  public void changeGameStatusTo(GameStatus gameStatus) {
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

  private int getCurrentTurnNumber() {
    return this.turn.getTurnNumber();
  }

  private void assertThatGameIsNotFinished(String msg) {
    if (this.status.equals(GameStatus.FINISHED) && !this.result.equals(GameResult.UNKNOWN)) {
      throw new PreconditionException(msg);
    }
  }
}
