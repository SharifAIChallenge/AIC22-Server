package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class Agent {
  private final int id;
  private int nodeId;
  private final String token;
  private final Team team;
  private final AgentType type;
  private boolean isDead;

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
