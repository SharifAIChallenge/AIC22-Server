package ir.sharif.aic.hideandseek.core.events;

import ir.sharif.aic.hideandseek.core.models.Chat;

public class AgentSentMessageEvent extends GameEvent {
  public AgentSentMessageEvent(Chat chat, double balance) {
    super(GameEventType.AGENT_SEND_MESSAGE);
    this.message =
        String.format(
            "agent with id: %d has send a message inside team: %s'%s chat box.",
            chat.getFromAgentId(), chat.getFromTeam().toString(), chat.getFromType().toString());

    this.addContext("agentId", chat.getFromAgentId());
    this.addContext("team", chat.getFromTeam());
    this.addContext("type", chat.getFromType());
    this.addContext("text", chat.getText());
    this.addContext("price", chat.getPrice());
    this.addContext("balance", balance);
  }
}
