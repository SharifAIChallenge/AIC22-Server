package ir.sharif.aic.hideandseek.core.event;

public class AgentReadinessDeclaredEvent extends GameEvent {
  public AgentReadinessDeclaredEvent(int agentId, String token, int startNodeId) {
    super(GameEventType.READINESS_DECLARATION);
    this.message =
        String.format(
            "a thief agent with id:%d has declared its readiness from node with id:%d",
            agentId, startNodeId);
    this.context.put("agentId", agentId);
    this.context.put("token", token);
    this.context.put("startNodeId", startNodeId);
  }

  public AgentReadinessDeclaredEvent(int agentId, String token) {
    super(GameEventType.READINESS_DECLARATION);
    this.message = String.format("a police agent with id:%d has declared its readiness", agentId);
    this.context.put("agentId", agentId);
    this.context.put("token", token);
  }
}
