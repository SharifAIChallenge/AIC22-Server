package ir.sharif.aic.hideandseek.core.events;

import lombok.Getter;

@Getter
public class PoliceCaughtThiefEvent extends GameEvent {
  private final int thiefId;

  public PoliceCaughtThiefEvent(int nodeId, int thiefId) {
    super(GameEventType.POLICES_CAUGHT_THIEVES);
    this.thiefId = thiefId;
    this.message =
        String.format("thief with id: %d has been arrested in node with id: %d ", thiefId, nodeId);
    this.addContext("thiefId", thiefId);
    this.addContext("nodeId", nodeId);
  }
}
