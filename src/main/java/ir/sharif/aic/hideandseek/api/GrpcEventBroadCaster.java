package ir.sharif.aic.hideandseek.api;

import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.events.*;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.java.Log;

import java.util.logging.Logger;

@AllArgsConstructor
@Builder
public class GrpcEventBroadCaster implements Watcher<GameEvent> {
    private final String agentToken;
    private final int agentId;
    private final GameService gameService;
    @Getter
    private final StreamObserver<HideAndSeek.GameView> observer;
    private boolean isClosed;
    private final Logger logger = Logger.getLogger(GrpcEventBroadCaster.class.getName());

    @Override
    public void watch(GameEvent event) {
        if (this.isClosed) {
            return;
        }
        if (!(event instanceof GameTurnChangedEvent ||
                event instanceof GameStatusChangedEvent ||
                event instanceof GameResultChangedEvent ||
                event instanceof AgentDeclaredReadinessEvent))
            return;
        logger.info("this log is from event : " + event.getType());
        var view = this.gameService.getView(this.agentToken);
        this.observer.onNext(view);
        if (isAgentArrested(event)) {
            this.observer.onCompleted();
        } else if (isGameFinished(event)) {
            this.observer.onCompleted();
            this.isClosed = true;
            new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(1000);
                        System.exit(0);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.run();
        }
    }

    private boolean isGameFinished(GameEvent event) {
        return event instanceof GameStatusChangedEvent e && e.changedToFinished();
    }

    private boolean isAgentArrested(GameEvent event) {
        return event instanceof PoliceCaughtThiefEvent e && e.getThiefId() == this.agentId;
    }

}
