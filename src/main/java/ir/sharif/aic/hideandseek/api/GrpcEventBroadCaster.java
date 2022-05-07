package ir.sharif.aic.hideandseek.api;

import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;
import lombok.Builder;

@AllArgsConstructor
@Builder
public class GrpcEventBroadCaster implements Watcher<GameEvent> {
  private final String agentToken;
  private final GameService gameService;
  private final StreamObserver<HideAndSeek.GameView> observer;
  private boolean isClosed;

  @Override
  public void watch(GameEvent event) {
    if (this.isClosed) {
      return;
    }

    var view = this.gameService.getView(this.agentToken);
    this.observer.onNext(view);

    if (event instanceof GameStatusChangedEvent e && e.changedToFinished()) {
      this.observer.onCompleted();
      this.isClosed = true;
    }
  }
}
