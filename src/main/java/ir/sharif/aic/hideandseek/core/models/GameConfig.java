package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.config.GameSettingsConfigurator;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class GameConfig {
  private final Map<String, Agent> agentMap = new HashMap<>();
  @Getter
  private final GameSettingsConfigurator.IncomeSettings incomeSettings;
  private final Graph graphMap;

  public GameConfig(Graph graphMap, GameSettingsConfigurator.IncomeSettings incomeSettings) {
    this.graphMap = graphMap;
    this.incomeSettings = incomeSettings;
  }

  public void addAgent(Agent newAgent) {
    newAgent.validate();

    if (this.agentMap.containsKey(newAgent.getToken()))
      throw new ValidationException("agent tokens must be unique", "agent.token");

    if (this.agentStream().anyMatch(agent -> agent.hasId(newAgent.getId())))
      throw new ValidationException("agent ids must be unique", "agent.id");

    this.agentMap.put(newAgent.getToken(), newAgent);
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

  public List<Agent> findAllPolice() {
    return this.agentStream().filter(agent -> agent.is(AgentType.POLICE)).toList();
  }

  public List<Agent> findAliveThieves() {
    return this.agentStream()
        .filter(agent -> agent.is(AgentType.THIEF) && agent.isAlive())
        .toList();
  }

  public boolean hasAliveThief(Team team){
    return findAllThiefAgentByTeam(team)
            .stream()
            .anyMatch(Agent::isAlive);
  }

  private List<Agent> findAllThiefAgentByTeam(Team team) {
    return agentStream().filter(agent -> agent.getTeam().equals(team) && agent.is(AgentType.THIEF)).toList();
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

  private Stream<Agent> agentStream() {
    return this.agentMap.values().stream();
  }

  public boolean checkTeamPoliceInNode(Team team, Node node) {
    return agentMap.values().stream()
        .anyMatch(
            agent ->
                agent.getNodeId() == node.getId()
                    && agent.getTeam() == team
                    && agent.getType().equals(AgentType.POLICE));
  }

  public List<Agent> findAllThievesByTeamAndNode(Team team, Node node) {
    return agentMap.values().stream()
        .filter(
            agent ->
                agent.getNodeId() == node.getId()
                    && agent.getTeam().equals(team)
                    && agent.getType().equals(AgentType.THIEF)
                    && !agent.isDead())
        .toList();
  }

  public List<Node> getAllNodes() {
    return graphMap.getAllNodes();
  }

  public Graph getGraphMap() {
    return this.graphMap;
  }

  public boolean everyAgentHasMovedThisTurn(AgentType agentType) {
    return agentMap.values().stream()
        .filter(agent -> agent.getType().equals(agentType) && !agent.isDead())
        .allMatch(Agent::isMovedThisTurn);
  }

  public HideAndSeek.GameConfig toProto() {
    return HideAndSeek.GameConfig.newBuilder()
        .setGraph(this.graphMap.toProto())
        .setIncomeSettings(this.incomeSettings.toProto())
        .build();
  }
}
