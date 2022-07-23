package ir.sharif.aic.hideandseek.core.events;

public class AgentBalanceChargedEvent extends GameEvent {
  public AgentBalanceChargedEvent(int agentId, double wage , double totalBalance) {
    super(GameEventType.AGENT_BALANCE_CHARGED);
    this.message =
        String.format("balance of agent with id: %d was charged %f units", agentId, wage);
    this.addContext("agentId", agentId);
    this.addContext("wage", wage);
    this.addContext("balance" , totalBalance);
  }
}
