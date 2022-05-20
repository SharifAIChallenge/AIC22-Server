package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.GameResult;

public class GameResultChangedEvent extends GameEvent {
  public GameResultChangedEvent(GameResult gameResult) {
    super(GameEventType.GAME_RESULT_CHANGED);
    this.message =
        String.format("Game result changed from %s to %s", GameResult.UNKNOWN, gameResult);
    this.addContext("game result", gameResult);
  }
}
