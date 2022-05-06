package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.lib.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class GameService {
  private final GameSpecs specs;
  private final Channel<GameEvent> eventChannel;
  private GameStatus status;
  private GameResult result;
  private Turn turn;

  public GameService(GameSpecs specs) {
    this.specs = specs;
    this.eventChannel = new AsyncChannel<>();
    this.status = GameStatus.PENDING;
    this.result = GameResult.UNKNOWN;
    this.turn = Turn.THIEF_TURN;
  }

  public void handle(DeclareReadinessCommand cmd) {
    cmd.validate();
    var agent = this.specs.findAgentByToken(cmd.getToken());
    agent.handle(cmd, this.eventChannel);

    if (this.specs.everyAgentIsReady()) {
      var fromStatus = this.status;
      this.status = GameStatus.ONGOING;
      this.eventChannel.push(new GameStatusChangedEvent(fromStatus, GameStatus.ONGOING));
    }
  }

  public synchronized void handle(WatchCommand cmd) {
    cmd.validate();
    this.specs.assertAgentExistsWithToken(cmd.getToken());
    this.eventChannel.addWatcher(cmd.getWatcher());
  }

  public HideAndSeek.GameView getView(String fromToken) {
    var viewerAgent = this.specs.findAgentByToken(fromToken);
    var visibleAgents =
        this.specs.findVisibleAgents(viewerAgent).stream()
            .map(Agent::toProto)
            .collect(Collectors.toList());

    return HideAndSeek.GameView.newBuilder()
        .setStatus(this.status.toProto())
        .setViewer(viewerAgent.toProto())
        .setResult(this.result.toProto())
        .setSpecs(this.specs.toProto())
        .setTurn(this.turn.toProto())
        .addAllVisibleAgents(visibleAgents)
        .build();
  }
}
