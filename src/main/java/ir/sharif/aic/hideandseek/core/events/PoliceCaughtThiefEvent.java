package ir.sharif.aic.hideandseek.core.events;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class PoliceCaughtThiefEvent extends GameEvent {
  public PoliceCaughtThiefEvent(int nodeId, int thiefId) {
    super(GameEventType.POLICES_CAUGHT_THIEVES);
    this.message =
        String.format("thief with id: %d has been arrested in node with id: %d ", thiefId, nodeId);
    this.addContext("thiefId", thiefId);
    this.addContext("nodeId", nodeId);
    log.info("EventType : {} , context : {}", this.getType(), this.context);
  }
}
