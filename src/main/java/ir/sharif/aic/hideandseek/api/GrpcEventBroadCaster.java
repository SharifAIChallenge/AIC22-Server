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

  @Override
  public void watch(GameEvent event) {
    var view = this.gameService.getView(this.agentToken);
    this.observer.onNext(view);

    if (event instanceof GameStatusChangedEvent
        && ((GameStatusChangedEvent) event).changedToFinished()) {
      this.observer.onCompleted();
    }
  }
}
