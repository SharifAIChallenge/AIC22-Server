package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.core.models.GameSpecs;
import ir.sharif.aic.hideandseek.core.models.GameStatus;
import ir.sharif.aic.hideandseek.lib.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import org.springframework.stereotype.Service;

@Service
public class GameService {
  private final GameSpecs specs;
  private final Channel<GameEvent> eventChannel;
  private GameStatus gameStatus;

  public GameService(GameSpecs specs) {
    this.specs = specs;
    this.eventChannel = new AsyncChannel<>();
    this.gameStatus = GameStatus.PENDING;
  }

  public void handle(DeclareReadinessCommand cmd) {
    cmd.validate();
    var agent = this.specs.findAgentByToken(cmd.getToken());
    agent.handle(cmd, this.eventChannel);

    if (this.specs.everyAgentIsReady()) {
      var fromStatus = this.gameStatus;
      this.gameStatus = GameStatus.ONGOING;
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

    return HideAndSeek.GameView.newBuilder()
        .setStatus(this.gameStatus.toProto())
        .setViewer(viewerAgent.toProto())
        .build();
  }
}
