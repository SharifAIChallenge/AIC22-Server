package ir.sharif.aic.hideandseek.core.events;

import lombok.Getter;

@Getter
public class AgentMovedEvent extends GameEvent {
  private int agentId;
  private int nodeId;
  private double balance;
  public AgentMovedEvent(int agentId, int nodeId, double balance) {
    super(GameEventType.AGENT_MOVEMENT);
    this.agentId = agentId;
    this.nodeId = nodeId;
    this.balance = balance;
    this.message =
        String.format("agent with id: %d decided to stay in node with id: %d", agentId, nodeId);

    this.addContext("agentId", agentId);
    this.addContext("fromNodeId", nodeId);
    this.addContext("toNodeId", nodeId);
    this.addContext("price", 0.0);
    this.addContext("balance" , balance);
  }

  public AgentMovedEvent(int agentId, int fromNodeId, int toNodeId, double price , double balance) {
    super(GameEventType.AGENT_MOVEMENT);
    this.agentId = agentId;
    this.nodeId = toNodeId;
    this.balance = balance;
    this.message =
        String.format(
            "agent with id: %d has moved from node with id: %d to node with id: %d",
            agentId, fromNodeId, toNodeId);

    this.addContext("agentId", agentId);
    this.addContext("fromNodeId", fromNodeId);
    this.addContext("toNodeId", toNodeId);
    this.addContext("price", price);
    this.addContext("balance" , balance);
  }
}
