package ir.sharif.aic.hideandseek.core.events;

public class AgentMovedEvent extends GameEvent {
    public AgentMovedEvent(int agentId, int fromNodeId, int toNodeId) {
        super(GameEventType.AGENT_MOVEMENT);
        this.message =
                String.format(
                        "agent with id: %d has moved from node with id: %d to node with id: %d",
                        agentId, fromNodeId, toNodeId);
        this.addContext("agentId", agentId);
        this.addContext("fromNodeId", fromNodeId);
        this.addContext("toNodeId", toNodeId);
    }
}
