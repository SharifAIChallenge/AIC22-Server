package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.config.GameConfigInjector;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

public class GameConfig {
    private final Map<String, Agent> agentMap = new HashMap<>();
    @Getter
    private final GameConfigInjector.IncomeSettings incomeSettings;
    @Getter
    private final GameConfigInjector.TurnSettings turnSettings;
    @Getter
    private final GameConfigInjector.ChatSettings chatSettings;
    private final Graph graphMap;
    @Getter
    private final int clientReadinessThresholdTimeMillisecond;
    @Getter
    private final int magicTurnTime;

    public GameConfig(
            Graph graphMap,
            GameConfigInjector.IncomeSettings incomeSettings,
            GameConfigInjector.TurnSettings turnSettings,
            GameConfigInjector.ChatSettings chatSettings,
            int clientReadinessThresholdTimeMillisecond,
            int magicTurnTime) {
        this.graphMap = graphMap;
        this.incomeSettings = incomeSettings;
        this.turnSettings = turnSettings;
        this.chatSettings = chatSettings;
        this.clientReadinessThresholdTimeMillisecond = clientReadinessThresholdTimeMillisecond;
        this.magicTurnTime = magicTurnTime;
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
     * <p>And excludes the viewer itself.
     *
     * @param viewer point of view
     * @return the list of agents that are visible from the given pov
     */
    public List<Agent> findVisibleAgentsByViewerAndTurn(Agent viewer, Turn turn) {
        List<Agent> visibleAgents = new ArrayList<>();
        var currentNode = graphMap.getNodeById(viewer.getNodeId());
        if (this.isVisibleTurn(turn)) {
            if (viewer.is(AgentType.POLICE) || viewer.is(AgentType.BATMAN)) {
                for (Agent agent : agentMap.values()) {
                    if (agent.equals(viewer))
                        continue;
                    var agentNode = graphMap.getNodeById(agent.getNodeId());
                    if (agent.is(AgentType.POLICE) || agent.is(AgentType.BATMAN))
                        visibleAgents.add(agent);
                    else if (agent.is(AgentType.THIEF)) {
                        if (currentNode.getVisibleRadiusXPoliceThief().contains(agentNode))
                            visibleAgents.add(agent);
                    } else {
                        if (currentNode.getVisibleRadiusYPoliceJoker().contains(agentNode))
                            visibleAgents.add(agent);
                    }

                }
            } else {
                getThiefAndJokerView(viewer, visibleAgents, currentNode);
            }
        } else {
            if (viewer.is(AgentType.POLICE) || viewer.is(AgentType.BATMAN)) {
                getPoliceAndBatmanNormalView(viewer, visibleAgents);
            } else {
                getThiefAndJokerView(viewer, visibleAgents, currentNode);
            }
        }
        return visibleAgents;

//        if (this.isVisibleTurn(turn)) {
//
//            return this.agentMap.values().stream().toList();
//        }
//
//        Predicate<Agent> criteria = agent -> agent.is(AgentType.POLICE);
//        criteria = criteria.or(agent -> agent.isInTheSameTeam(viewer) && agent.is(AgentType.THIEF));
//        criteria = criteria.and(agent -> !agent.equals(viewer));
//
//        return this.agentStream().filter(criteria).toList();
    }

    private void getPoliceAndBatmanNormalView(Agent viewer, List<Agent> visibleAgents) {
        for (Agent agent : agentMap.values()) {
            if (agent.equals(viewer))
                continue;
            if (agent.is(AgentType.POLICE) || agent.is(AgentType.BATMAN))
                visibleAgents.add(agent);
        }
    }

    private void getThiefAndJokerView(Agent viewer, List<Agent> visibleAgents, Node currentNode) {
        for (Agent agent : agentMap.values()) {
            if (agent.equals(viewer))
                continue;
            if (agent.is(AgentType.POLICE))
                visibleAgents.add(agent);
            else if (agent.is(AgentType.BATMAN)) {
                var agentNode = graphMap.getNodeById(agent.getNodeId());
                if (currentNode.getVisibleRadiusZThiefBatman().contains(agentNode))
                    visibleAgents.add(agent);
            }
        }
    }

    private boolean isVisibleTurn(Turn turn) {
        return this.turnSettings.getVisibleTurns().contains(turn.getTurnNumber());
    }

    public Path findPath(int sourceNodeId, int destinationNodeId) {
        return this.graphMap.findPath(sourceNodeId, destinationNodeId);
    }

    public List<Agent> findAllPolice() {
        return this.agentStream().filter(agent -> agent.is(AgentType.POLICE) || agent.is(AgentType.BATMAN)).toList();
    }

    public List<Agent> findAliveThieves() {
        return this.agentStream()
                .filter(agent -> (agent.is(AgentType.THIEF) || agent.is(AgentType.JOKER)) && agent.isAlive())
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

    public int checkNumberOfPoliceInNodeByTeam(Team team, Node node) {
        return agentMap.values().stream()
                .filter(
                        agent ->
                                agent.getNodeId() == node.getId()
                                        && agent.getTeam() == team
                                        && agent.getType().equals(AgentType.POLICE)).toList().size();
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

    public Optional<Agent> findJokerWithTeam(Team team) {
        return agentMap.values().stream().filter(agent -> agent.getType().equals(AgentType.JOKER) && agent.getTeam().equals(team)).findFirst();
    }


    public Optional<Agent> findBatmanWithTeam(Team team) {
        return agentMap.values().stream().filter(agent -> agent.getType().equals(AgentType.BATMAN) && agent.getTeam().equals(team)).findFirst();
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
