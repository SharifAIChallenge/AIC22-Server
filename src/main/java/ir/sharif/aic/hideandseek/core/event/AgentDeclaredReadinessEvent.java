package ir.sharif.aic.hideandseek.core.event;

public class AgentDeclaredReadinessEvent extends GameEvent {
  public AgentDeclaredReadinessEvent(int agentId, String token, int startNodeId) {
    super(GameEventType.READINESS_DECLARATION);
    this.message =
        String.format(
            "a thief agent with id:%d has declared its readiness from node with id:%d",
            agentId, startNodeId);
    this.addContext("agentId", agentId);
    this.addContext("token", token);
    this.addContext("startNodeId", startNodeId);
  }

  public AgentDeclaredReadinessEvent(int agentId, String token) {
    super(GameEventType.READINESS_DECLARATION);
    this.message = String.format("a police agent with id:%d has declared its readiness", agentId);
    this.addContext("agentId", agentId);
    this.addContext("token", token);
  }
}
