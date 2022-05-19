package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.exceptions.PreconditionException;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Builder
@AllArgsConstructor
public class GameRepository {
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

    this.agentMap.put(newAgent.getToken() , newAgent);
  }

  public Agent findAgentByToken(String token) {
    TokenValidator.validate(token, "token");
    this.assertAgentExistsWithToken(token);
    return this.agentMap.get(token);
  }

  /**
   * Returns the visible agents to a certain viewer which includes:
   *
   * <ol>
   *   <li>all the police agents
   *   <li>all the thief teammates
   * </ol>
   *
   * And excludes the viewer itself.
   *
   * @param viewer point of view
   * @return the list of agents that are visible from the given pov
   */
  public List<Agent> findVisibleAgents(Agent viewer) {
    Predicate<Agent> criteria = agent -> agent.is(AgentType.POLICE);
    criteria = criteria.or(agent -> agent.isInTheSameTeam(viewer) && agent.is(AgentType.THIEF));
    criteria = criteria.and(agent -> !agent.equals(viewer));

    return this.agentStream().filter(criteria).toList();
  }

  public Path findPath(int sourceNodeId, int destinationNodeId) {
    return this.graphMap.findPath(sourceNodeId, destinationNodeId);
  }

  public void assertAgentExistsWithToken(String token) {
    if (!this.agentMap.containsKey(token))
      throw new NotFoundException(Agent.class.getSimpleName(), Map.of("token", token));
  }

  public boolean everyAgentIsReady() {
    return this.agentStream().allMatch(Agent::isReady);
  }


  public List<Agent> getAllAgents() {
    return this.agentMap.values().stream().toList();
  }

  public HideAndSeek.GameSpecs getSpecs() {
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
