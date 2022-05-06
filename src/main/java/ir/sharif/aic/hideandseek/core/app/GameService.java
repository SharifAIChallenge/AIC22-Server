package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.channel.Channel;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.event.AgentDeclaredReadinessEvent;
import ir.sharif.aic.hideandseek.core.event.GameEvent;
import ir.sharif.aic.hideandseek.core.models.GameSpecs;
import org.springframework.stereotype.Service;

@Service
public class GameService {
  private final GameSpecs specs;
  private final Channel<GameEvent> eventChannel;

  public GameService(GameSpecs specs) {
    this.specs = specs;
    this.eventChannel = new AsyncChannel<>();
  }

  public void handle(DeclareReadinessCommand cmd) {
    cmd.validate();
    var agent = this.specs.findAgentByToken(cmd.getToken());
    agent.handle(cmd);

    GameEvent event = null;
    switch (agent.getType()) {
      case POLICE:
        event = new AgentDeclaredReadinessEvent(agent.getId(), agent.getToken());
        break;
      case THIEF:
        event =
            new AgentDeclaredReadinessEvent(agent.getId(), agent.getToken(), cmd.getStartNodeId());
    }

    this.eventChannel.push(event);
  }
}
