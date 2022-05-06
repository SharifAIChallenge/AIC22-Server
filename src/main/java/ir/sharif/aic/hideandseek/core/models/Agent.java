package ir.sharif.aic.hideandseek.core.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exception.ValidationException;
import lombok.Data;

@Data
public class Agent {
  private Integer id;
  private Integer nodeId;
  private String token;
  private Team team;
  private AgentType type;
  @JsonIgnore private boolean isDead = false;

  public void validate() {
    if (this.id == null) throw new ValidationException("agent id cannot be null", "agent.id");
    if (this.id <= 0) throw new ValidationException("agent id must be positive", "agent.id");
    GraphValidator.validateNodeId(this.nodeId, "agent.nodeId");
    TokenValidator.validate(this.token, "agent.token");
    if (this.team == null) throw new ValidationException("agent team cannot be null", "agent.team");
    if (this.type == null) throw new ValidationException("agent type cannot be null", "agent.type");
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
        .setIsDead(this.isDead)
        .build();
  }
}
