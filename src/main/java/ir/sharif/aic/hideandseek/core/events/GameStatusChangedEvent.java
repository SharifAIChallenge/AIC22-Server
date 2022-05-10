package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.GameStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameStatusChangedEvent extends GameEvent {

    public GameStatusChangedEvent(GameStatus fromStatus, GameStatus toStatus) {
        super(GameEventType.STATUS_CHANGE);
        this.message =
                String.format(
                        "game changed status from %s to %s", fromStatus.toString(), toStatus.toString());
        this.addContext("fromStatus", fromStatus);
        this.addContext("toStatus", toStatus);
        log.info("EventType : {} , context : {}", this.getType(), this.context);
    }

    public boolean changedToFinished() {
        return this.context.containsKey("toStatus")
                && this.context.get("toStatus").equals(GameStatus.FINISHED.toString());
    }
}
