package ir.sharif.aic.hideandseek.api;

import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.events.GameStatusChangedEvent;
import ir.sharif.aic.hideandseek.core.events.PoliceCaughtThieves;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
public class GrpcEventBroadCaster implements Watcher<GameEvent> {
    private final String agentToken;
    private final int agentId;
    private final GameService gameService;
    @Getter
    private final StreamObserver<HideAndSeek.GameView> observer;
    private boolean isClosed;

    @Override
    public void watch(GameEvent event) {
        if (this.isClosed) {
            return;
        }

        var view = this.gameService.getView(this.agentToken);
        this.observer.onNext(view);
        if (isAgentArrested(event)) {
            this.observer.onCompleted();
        } else if (isGameFinished(event)) {
            this.observer.onCompleted();
            this.isClosed = true;
        }
    }

    private boolean isGameFinished(GameEvent event) {
        return event instanceof GameStatusChangedEvent e && e.changedToFinished();
    }

    private boolean isAgentArrested(GameEvent event) {
        return event instanceof PoliceCaughtThieves e && e.getThiefId() == this.agentId;
    }
}
