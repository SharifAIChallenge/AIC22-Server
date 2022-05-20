package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.TurnType;

public class GameTurnChangedEvent extends GameEvent {
  public GameTurnChangedEvent(TurnType toTurnType ,Integer toTurnNumber) {
    super(GameEventType.TURN_CHANGE);
    this.addContext("toTurn", toTurnType);
    this.addContext("toTurnNumber" ,toTurnNumber);
  }
}
