package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.Agent;

public class AgentDeclaredReadinessEvent extends GameEvent {
  public AgentDeclaredReadinessEvent(Agent agent) {
    super(GameEventType.READINESS_DECLARATION);
    this.message = String.format("agent with id:%d has declared its readiness", agent.getId());
    this.addContext("agentId", agent.getId());
    this.addContext("token", agent.getToken());
    this.addContext("nodeId" , agent.getNodeId());
    this.addContext("team" , agent.getTeam());
    this.addContext("type" , agent.getType());
    this.addContext("balance" , agent.getBalance());
  }

}
