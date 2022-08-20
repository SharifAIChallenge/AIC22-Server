package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.config.GameConfigInjector;
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
  @Getter private final GameConfigInjector.IncomeSettings incomeSettings;
  @Getter private final GameConfigInjector.TurnSettings turnSettings;
  @Getter private final GameConfigInjector.ChatSettings chatSettings;
  private final Graph graphMap;
  @Getter private final int clientReadinessThresholdTimeMillisecond;

  public GameConfig(
          Graph graphMap,
          GameConfigInjector.IncomeSettings incomeSettings,
          GameConfigInjector.TurnSettings turnSettings,
          GameConfigInjector.ChatSettings chatSettings,
          int clientReadinessThresholdTimeMillisecond) {
    this.graphMap = graphMap;
    this.incomeSettings = incomeSettings;
    this.turnSettings = turnSettings;
    this.chatSettings = chatSettings;
    this.clientReadinessThresholdTimeMillisecond = clientReadinessThresholdTimeMillisecond;
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

  public Agent findAgentById(Integer id){
    for(Agent agent : agentMap.values()){
      if(agent.getId().equals(id))
        return agent;
    }
    throw new NotFoundException(Agent.class.getSimpleName(), Map.of("ID", id));
  }

  /**
   * Returns the visible agents to a certain viewer which includes:
   *
   * <ol>
   *   <li>all the police agents
   *   <li>all the thief teammates
   * </ol>
   *
   * <p>And excludes the viewer itself.
   *
   * @param viewer point of view
   * @return the list of agents that are visible from the given pov
   */
  public List<Agent> findVisibleAgentsByViewerAndTurn(Agent viewer, Turn turn) {
    if (this.isVisibleTurn(turn)) {
      return this.agentMap.values().stream().toList();
    }

    Predicate<Agent> criteria = agent -> agent.is(AgentType.POLICE);
    criteria = criteria.or(agent -> agent.isInTheSameTeam(viewer) && agent.is(AgentType.THIEF));
    criteria = criteria.and(agent -> !agent.equals(viewer));

    return this.agentStream().filter(criteria).toList();
  }

  private boolean isVisibleTurn(Turn turn) {
    return this.turnSettings.getVisibleTurns().contains(turn.getTurnNumber());
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

  public boolean hasAliveThief(Team team) {
    return findAllThievesByTeam(team).stream().anyMatch(Agent::isAlive);
  }

  public List<Agent> findAllThievesByTeam(Team team) {
    return agentStream()
        .filter(agent -> agent.getTeam().equals(team) && agent.is(AgentType.THIEF))
        .toList();
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

  public boolean everyAgentHasMovedThisTurn(AgentType agentType) {
    return agentMap.values().stream()
        .filter(agent -> agent.getType().equals(agentType) && !agent.isDead())
        .allMatch(Agent::isMovedThisTurn);
  }

  public int getMaxTurns() {
    return this.turnSettings.getMaxTurns();
  }

  public HideAndSeek.GameConfig toProto() {
    return HideAndSeek.GameConfig.newBuilder()
        .setGraph(this.graphMap.toProto())
        .setIncomeSettings(this.incomeSettings.toProto())
        .setTurnSettings(this.turnSettings.toProto())
        .setChatSettings(this.chatSettings.toProto())
        .build();
  }
}
