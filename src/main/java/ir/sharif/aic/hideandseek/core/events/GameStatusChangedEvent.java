package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.GameStatus;

public class GameStatusChangedEvent extends GameEvent {
  public GameStatusChangedEvent(GameStatus fromStatus, GameStatus toStatus) {
    super(GameEventType.STATUS_CHANGE);
    this.message =
        String.format(
            "game changed status from %s to %s", fromStatus.toString(), toStatus.toString());
    this.addContext("fromStatus", fromStatus);
    this.addContext("toStatus", toStatus);
  }

  public boolean changedToFinished() {
    return this.context.containsKey("toStatus")
        && this.context.get("toStatus").equals(GameStatus.FINISHED.toString());
  }
}
