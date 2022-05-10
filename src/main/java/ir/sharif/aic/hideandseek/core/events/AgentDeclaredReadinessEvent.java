package ir.sharif.aic.hideandseek.core.events;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentDeclaredReadinessEvent extends GameEvent {
    public AgentDeclaredReadinessEvent(int agentId, String token) {
        super(GameEventType.READINESS_DECLARATION);
        this.message = String.format("agent with id:%d has declared its readiness", agentId);
        this.addContext("agentId", agentId);
        this.addContext("token", token);
        log.info("EventType : {} , context : {}", this.getType(), this.context);
    }

    public void startFromNodeId(int nodeId) {
        this.addContext("startNodeId", nodeId);
    }
}
