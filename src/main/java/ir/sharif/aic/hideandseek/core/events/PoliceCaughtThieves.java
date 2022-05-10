package ir.sharif.aic.hideandseek.core.events;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class PoliceCaughtThieves extends GameEvent {
    private final Integer thiefId;
    public PoliceCaughtThieves(int nodeId, int thiefId) {
        super(GameEventType.POLICES_CAUGHT_THIEVES);
        this.thiefId = thiefId;
        this.message = String.format(
                "Thief with id: %d has arrested in node with id: %d ",
                thiefId, nodeId);
        this.addContext("thiefId", thiefId);
        this.addContext("nodeId", nodeId);
        log.info("EventType : {} , context : {}", this.getType(), this.context);
    }


}
