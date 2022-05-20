package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.Turn;

public class GameTurnChangedEvent extends GameEvent {
    public GameTurnChangedEvent(Turn toTurn) {
        super(GameEventType.TURN_CHANGE);
        this.addContext("toTurn", toTurn);
    }
}
