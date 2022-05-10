package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.Turn;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameTurnChangedEvent extends GameEvent {
    public GameTurnChangedEvent(Turn toTurn) {
        super(GameEventType.TURN_CHANGE);
        this.addContext("toTurn", toTurn);
        log.info("EventType : {} , context : {}", this.getType(), this.context);
    }
}
