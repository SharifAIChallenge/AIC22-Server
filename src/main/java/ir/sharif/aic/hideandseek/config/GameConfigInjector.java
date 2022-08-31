package ir.sharif.aic.hideandseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.models.*;
import ir.sharif.aic.hideandseek.core.watchers.EventLogger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Configuration
@Slf4j
public class GameConfigInjector {
    private static final String JAVA_EXEC_CMD = "java -jar";
    private static final Logger LOGGER = LoggerFactory.getLogger(GameConfigInjector.class);
    private static String FIRST_TEAM_PATH = null;
    private static String FIRST_TEAM_NAME = null;
    private static String SECOND_TEAM_PATH = null;
    private static String SECOND_TEAM_NAME = null;
    private static String GAME_CONFIG_PATH = null;
    private static String MAP_PATH = null;
    private final static int INF = Integer.MAX_VALUE;


    public static void handleCMDArgs(String[] args) {
        int namedArgsCount = 0;
        for (String arg : args) {
            namedArgsCount += handleArg(arg) ? 1 : 0;
        }
        try {
            GAME_CONFIG_PATH = args[namedArgsCount];
            MAP_PATH = args[namedArgsCount+1];
        } catch (Exception ignore) {
            LOGGER.error("Invalid args.");
        }

        if ((FIRST_TEAM_PATH == null || SECOND_TEAM_PATH == null)) {
            LOGGER.error("No path for clients");
            System.exit(100);
        }

        if (GAME_CONFIG_PATH == null) {
            LOGGER.error("No path for game config.");
        }

        if (MAP_PATH == null) {
            LOGGER.warn("No path for map.json");
        }

        var logger = java.util.logging.Logger.getLogger(EventLogger.class.getName());
        if (FIRST_TEAM_NAME != null && SECOND_TEAM_NAME != null) {
            logger.info(String.format("{\"first\":\"%s\", \"second\":\"%s\"}", FIRST_TEAM_NAME, SECOND_TEAM_NAME));
        } else {
            logger.info("{\"first\":\"FIRST\", \"second\":\"SECOND\"}");
        }
    }

    private static boolean handleArg(String arg) {
        String[] split = arg.split("=");
        try {
            if (split[0].equals("--first-team")) {
                FIRST_TEAM_PATH = split[1];
            } else if (split[0].equals("--second-team")) {
                SECOND_TEAM_PATH = split[1];
            } else if (split[0].equals("--first-team-name")) {
                FIRST_TEAM_NAME = split[1];
            } else if (split[0].equals("--second-team-name")) {
                SECOND_TEAM_NAME = split[1];
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static String createRunCMD(String path) {
        path = path.strip();
        if (path.contains(".jar")) {
            return JAVA_EXEC_CMD + " " + path;
        }
        return path;
    }

    @Bean
    public GameConfig createGameConfig() throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory());
        try {
            var settings = mapper.readValue(new File(GAME_CONFIG_PATH), GameSettings.class);
            var graph = new Graph(settings.graph.visibleRadiusXPoliceThief, settings.graph.visibleRadiusYPoliceJoker, settings.graph.visibleRadiusZThiefBatman);
            if (MAP_PATH != null) {
                try {
                    Scanner scanner = new Scanner(new File(MAP_PATH));
                    var map = scanner.nextLine();
                    LOGGER.info(map);
                    GraphicLogger.getInstance().appendLog(map);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            AddRadiusToNodes(settings.graph, settings.getGraph().visibleRadiusXPoliceThief, settings.getGraph().visibleRadiusYPoliceJoker, settings.getGraph().visibleRadiusZThiefBatman);
            settings.graph.nodes.forEach(graph::addNode);
            settings.graph.paths.forEach(path -> addPathToGraph(settings.graph.nodes, graph, path));

            var config = new GameConfig(graph, settings.income, settings.turnSettings, settings.chatSettings,
                    clientReadinessThresholdTimeMillisecond, magicTurnTime);
            log.info("clientReadinessThresholdTimeMillisecond is set to {}", clientReadinessThresholdTimeMillisecond);
            log.info("magicTurnTime is set to {}", magicTurnTime);
            settings.agents.forEach(config::addAgent);
            var firstTeamRunCMD = createRunCMD(FIRST_TEAM_PATH);
            var secondTeamRunCMD = createRunCMD(SECOND_TEAM_PATH);
            settings.agents.forEach(agent -> {
                var runCommand = agent.getTeam().equals(Team.FIRST) ? firstTeamRunCMD : secondTeamRunCMD;
                var client = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(4000);
                            Process p = Runtime.getRuntime().exec(runCommand + ' ' + agent.getToken());
                            var error = p.getErrorStream();
                            var input = p.getInputStream();

                            new Thread(() -> {
                                InputStreamReader inputStreamReader = new InputStreamReader(input);
                                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                                String line;
                                try {
                                    while ((line = bufferedReader.readLine()) != null) {
                                        LOGGER.info("Client {} from team {} log: {}", agent.getId(), agent.getTeam(), line);
                                    }
                                } catch (IOException e) {
                                    LOGGER.error(e.getMessage());
                                }
                            }).start();

                            new Thread(() -> {
                                var errStringBuilder = new StringBuilder();
                                errStringBuilder.append("Client ").append(agent.getId())
                                        .append(" from ").append(agent.getTeam()).append(" team:\n");
                                var isrerror = new InputStreamReader(error);
                                var bre = new BufferedReader(isrerror);
                                String linee;
                                try {
                                    while ((linee = bre.readLine()) != null) {
                                        errStringBuilder.append(linee).append("\n");
                                        LOGGER.error("Client {} from team {} error: {}", agent.getId(), agent.getTeam(), linee);
                                    }
                                    System.err.println(errStringBuilder);
                                } catch (IOException e) {
                                    LOGGER.error(e.getMessage());
                                }
                            }).start();

                        } catch (IOException e) {
                            LOGGER.error(e.getMessage());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                client.start();
            });


            return config;

        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw exception;
        }
    }

    private void AddRadiusToNodes(GraphSettings graphSettings, int XRadius, int YRadius, int ZRadius) {
        var baseGraph = createFloydWarshallGraph(graphSettings);
        baseGraph = calculateVisibleRadiusGraph(baseGraph, graphSettings.nodes.size());
        addVisibleRadiusToNodes(graphSettings, baseGraph, XRadius, YRadius, ZRadius);
    }

    private int[][] createFloydWarshallGraph(GraphSettings graphSettings) {
        var nodes = graphSettings.getNodes();
        final var V = nodes.size();
        int[][] graph = new int[V][V];
        for (int i = 0; i < V; i++) {
            for (int j = 0; j < V; j++) {
                graph[i][j] = checkIfAPathWeightIsZero(graphSettings, i, j);
            }
        }
        return graph;
    }


    private int[][] calculateVisibleRadiusGraph(int[][] graph, int V) {
        int dist[][] = new int[V][V];
        int i, j, k;
        for (i = 0; i < V; i++)
            for (j = 0; j < V; j++)
                dist[i][j] = graph[i][j];
        for (k = 0; k < V; k++) {
            // Pick all vertices as source one by one
            for (i = 0; i < V; i++) {
                // Pick all vertices as destination for the
                // above picked source
                for (j = 0; j < V; j++) {
                    // If vertex k is on the shortest path from
                    // i to j, then update the value of dist[i][j]
                    if (dist[i][k] + dist[k][j] < dist[i][j])
                        dist[i][j] = dist[i][k] + dist[k][j];
                }
            }
        }
        return dist;
    }

    private void addVisibleRadiusToNodes(GraphSettings graphSettings, int[][] graph, int XRadius, int YRadius, int ZRadius) {
        var nodes = graphSettings.getNodes();
        nodes.forEach(e -> {
            var nodes_from_e = graph[e.getId() - 1];
            for (int i = 0; i < nodes_from_e.length; i++) {
                int distance = graph[e.getId() - 1][i];
                var node = findNodeById(nodes, i + 1);
                if (distance <= XRadius)
                    e.getVisibleRadiusXPoliceThief().add(node);
                if (distance <= YRadius)
                    e.getVisibleRadiusYPoliceJoker().add(node);
                if (distance <= ZRadius)
                    e.getVisibleRadiusZThiefBatman().add(node);

            }
        });
    }


    private int checkIfAPathWeightIsZero(GraphSettings graphSettings, int src, int dest) {
        if (src == dest)
            return 0;
        var paths = graphSettings.getPaths();
        var path = paths.stream().filter(e -> (
                (e.getFirstNodeId() == src && e.getSecondNodeId() == dest) ||
                        (e.getFirstNodeId() == dest && e.getSecondNodeId() == src))).findFirst();
        if (path.isPresent() && path.get().getPrice() == 0)
            return 1;
        return INF;

    }

    private Node findNodeById(List<Node> nodes, int nodeId) {
        return nodes.stream().filter(node -> node.getId() == nodeId).findFirst().orElseThrow(() -> new NotFoundException(Node.class.getSimpleName(), nodeId));
    }

    private void addPathToGraph(List<Node> nodes, Graph graph, Path path) {
        graph.addPath(path, this.findNodeById(nodes, path.getFirstNodeId()), this.findNodeById(nodes, path.getSecondNodeId()));
    }

    @Data
    private static class GraphSettings {
        private List<Node> nodes;
        private List<Path> paths;
        private int visibleRadiusXPoliceThief;
        private int visibleRadiusYPoliceJoker;
        private int visibleRadiusZThiefBatman;
    }

    @Data
    public static class IncomeSettings {
        private Double policeIncomeEachTurn;
        private Double thiefIncomeEachTurn;

        public HideAndSeek.IncomeSettings toProto() {
            return HideAndSeek.IncomeSettings.newBuilder().setPoliceIncomeEachTurn(this.policeIncomeEachTurn).setThievesIncomeEachTurn(this.thiefIncomeEachTurn).build();
        }
    }

    @Data
    public static class TurnSettings {
        private Integer maxTurns;
        private List<Integer> visibleTurns;

        public HideAndSeek.TurnSettings toProto() {
            return HideAndSeek.TurnSettings.newBuilder().setMaxTurns(this.maxTurns).addAllVisibleTurns(this.visibleTurns).build();
        }
    }

    @Data
    public static class ChatSettings {
        private Integer chatBoxMaxSize;
        private Double chatCostPerCharacter;

        public HideAndSeek.ChatSettings toProto() {
            return HideAndSeek.ChatSettings.newBuilder().setChatBoxMaxSize(this.chatBoxMaxSize).setChatCostPerCharacter(this.chatCostPerCharacter).build();
        }
    }

    @Value("${clientReadinessThresholdTimeMillisecond:7000}")
    private int clientReadinessThresholdTimeMillisecond;

    @Value("${magicTurnTime: 1000}")
    private int magicTurnTime;

    @Data
    private static class GameSettings {
        private List<Agent> agents = new ArrayList<>();
        private GraphSettings graph = new GraphSettings();
        private IncomeSettings income = new IncomeSettings();
        private TurnSettings turnSettings = new TurnSettings();
        private ChatSettings chatSettings = new ChatSettings();
    }
}
