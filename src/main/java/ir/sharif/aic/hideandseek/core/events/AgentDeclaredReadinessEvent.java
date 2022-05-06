package ir.sharif.aic.hideandseek.core.events;

public class AgentDeclaredReadinessEvent extends GameEvent {
  public AgentDeclaredReadinessEvent(int agentId, String token) {
    super(GameEventType.READINESS_DECLARATION);
    this.message = String.format("an agent with id:%d has declared its readiness", agentId);
    this.addContext("agentId", agentId);
    this.addContext("token", token);
  }

  public void startFromNodeId(int nodeId) {
    this.addContext("startNodeId", nodeId);
  }
}
