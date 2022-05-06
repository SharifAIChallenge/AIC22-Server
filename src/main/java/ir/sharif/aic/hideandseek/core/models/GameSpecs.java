package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exception.NotFoundException;
import ir.sharif.aic.hideandseek.core.exception.PreconditionException;
import ir.sharif.aic.hideandseek.core.exception.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
public class GameSpecs {
  private final int maxThiefCount;
  private final int maxPoliceCount;
  @Builder.Default private final Map<String, Agent> agentMap = new HashMap<>();
  private final Graph graphMap;

  public void addAgent(Agent newAgent) {
    newAgent.validate();

    if (this.agentMap.containsKey(newAgent.getToken()))
      throw new ValidationException("agent tokens must be unique", "agent.token");

    if (this.agentStream().anyMatch(agent -> agent.hasId(newAgent.getId())))
      throw new ValidationException("agent ids must be unique", "agent.id");

    var sameTeamCount =
        this.agentStream()
            .filter(agent -> agent.isInTheSameTeam(newAgent))
            .filter(agent -> agent.is(newAgent.getType()))
            .count();
    var limit = newAgent.getType() == AgentType.POLICE ? this.maxPoliceCount : this.maxThiefCount;

    if (sameTeamCount == limit) throw new PreconditionException("MAX_TEAM_LIMIT_EXCEEDED");
  }

  public Agent findAgentByToken(String token) {
    TokenValidator.validate(token, "token");

    if (!this.agentMap.containsKey(token))
      throw new NotFoundException(Agent.class.getSimpleName(), Map.of("token", token));

    return this.agentMap.get(token);
  }

  public HideAndSeek.GameSpecs toProto() {
    return HideAndSeek.GameSpecs.newBuilder()
        .setMaxPoliceCount(this.maxPoliceCount)
        .setMaxThiefCount(this.maxThiefCount)
        .setGraphMap(graphMap.toProto())
        .build();
  }

  private Stream<Agent> agentStream() {
    return this.agentMap.values().stream();
  }
}
