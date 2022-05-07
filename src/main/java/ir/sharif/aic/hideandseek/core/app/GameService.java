package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.DoActionCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.core.exceptions.PreconditionException;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.lib.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class GameService {
  private final GameRepository specs;
  private final Channel<GameEvent> eventChannel;
  private GameStatus status;
  private GameResult result;
  private AgentType turn;

  public GameService(GameRepository specs) {
    this.specs = specs;
    this.eventChannel = new AsyncChannel<>();
    this.status = GameStatus.PENDING;
    this.result = GameResult.UNKNOWN;
    this.turn = AgentType.THIEF;
  }

  public void handle(DeclareReadinessCommand cmd) {
    cmd.validate();
    var agent = this.specs.findAgentByToken(cmd.getToken());
    agent.apply(cmd, this.eventChannel);

    if (this.specs.everyAgentIsReady()) {
      var fromStatus = this.status;
      this.status = GameStatus.ONGOING;
      this.eventChannel.push(new GameStatusChangedEvent(fromStatus, GameStatus.ONGOING));
    }
  }

  public void handle(WatchCommand cmd) {
    cmd.validate();
    this.specs.assertAgentExistsWithToken(cmd.getToken());
    this.eventChannel.addWatcher(cmd.getWatcher());
  }

  public void handle(DoActionCommand cmd) {
    cmd.validate();
    var agent = this.specs.findAgentByToken(cmd.getToken());

    if (!agent.is(this.turn)) throw new PreconditionException("it's not your turn yet.");

    if (!agent.isReady())
      throw new PreconditionException("you have not declared your readiness yet.");

    var src = agent.getNodeId();
    var dst = cmd.getToNodeId();

    if (src == dst) {
      return;
    }

    var path = this.specs.findPath(src, dst);
    agent.moveAlong(path);
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
        .setSpecs(this.specs.getSpecs())
        .setTurn(this.turn.toProto())
        .addAllVisibleAgents(visibleAgents)
        .build();
  }
}
