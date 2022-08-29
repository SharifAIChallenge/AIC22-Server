package ir.sharif.aic.hideandseek.core.events;

public class JokerGotArrested extends GameEvent {

    public JokerGotArrested(int nodeId, int jokerId) {
        super(GameEventType.JOKER_GOT_ARRESTED);
        this.message = String.format("thief with id: %d has been arrested in node with id: %d ", jokerId, nodeId);
        this.addContext("jokerId", jokerId);
        this.addContext("nodeId", nodeId);
    }
}
