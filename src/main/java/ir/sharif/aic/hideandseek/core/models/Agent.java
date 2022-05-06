package ir.sharif.aic.hideandseek.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.events.AgentDeclaredReadinessEvent;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.lib.channel.Channel;
import ir.sharif.aic.hideandseek.core.exceptions.InternalException;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import lombok.Data;

@Data
public class Agent {
  private Integer id;
  private Integer nodeId;
  private String token;
  private Team team;
  private AgentType type;
  @JsonIgnore private boolean ready = false;
  @JsonIgnore private boolean dead = false;
  @JsonIgnore private boolean visible = true;

  public synchronized void handle(DeclareReadinessCommand cmd, Channel<GameEvent> eventChannel) {
    // validations
    cmd.validate();
    if (!(this.token != null && this.token.equals(cmd.getToken())))
      throw new InternalException("command token does not match agent token");

    if (this.ready) return;

    if (this.is(AgentType.THIEF)) this.nodeId = cmd.getStartNodeId();

    // side effects
    this.ready = true;

    // broadcast event
    var event = new AgentDeclaredReadinessEvent(this.id, this.token);
    if (this.is(AgentType.THIEF)) event.startFromNodeId(cmd.getStartNodeId());

    eventChannel.push(event);
  }

  public void validate() {
    if (this.id == null) throw new ValidationException("agent id cannot be null", "agent.id");
    if (this.id <= 0) throw new ValidationException("agent id must be positive", "agent.id");
    GraphValidator.validateNodeId(this.nodeId, "agent.nodeId");
    TokenValidator.validate(this.token, "agent.token");
    if (this.team == null) throw new ValidationException("agent team cannot be null", "agent.team");
    if (this.type == null) throw new ValidationException("agent type cannot be null", "agent.type");
    if (this.type.equals(AgentType.POLICE) && this.nodeId == null)
      throw new ValidationException("police node id cannot be null", "agent.nodeId");
  }

  public boolean hasId(int anId) {
    return this.id == anId;
  }

  public boolean isInTheSameTeam(Agent agent) {
    return this.team != null && this.team.equals(agent.getTeam());
  }

  public boolean is(AgentType type) {
    return this.type != null && this.type.equals(type);
  }

  public HideAndSeek.Agent toProto() {
    return HideAndSeek.Agent.newBuilder()
        .setId(this.id)
        .setNodeId(this.nodeId)
        .setTeam(this.team.toProto())
        .setType(this.type.toProto())
        .setIsDead(this.dead)
        .build();
  }
}
