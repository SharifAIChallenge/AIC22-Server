package ir.sharif.aic.hideandseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.models.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger LOGGER = LoggerFactory.getLogger(GameConfigInjector.class);
    private static String FIRST_TEAM_PATH = null;
    private static String SECOND_TEAM_PATH = null;
    private static final String JAVA_EXEC_CMD = "java -jar";
    private static String GAME_CONFIG_PATH = null;
    private static String MAP_PATH = null;

    @Bean
    public GameConfig createGameConfig() throws IOException {
        var graph = new Graph();
        var mapper = new ObjectMapper(new YAMLFactory());
        try {
            var settings = mapper.readValue(new File(GAME_CONFIG_PATH), GameSettings.class);
            if(MAP_PATH != null){
                try {
                    Scanner scanner = new Scanner(new File(MAP_PATH));
                    LOGGER.info(scanner.nextLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            settings.graph.nodes.forEach(graph::addNode);
            settings.graph.paths.forEach(path -> addPathToGraph(settings.graph.nodes, graph, path));

            var config =
                    new GameConfig(graph, settings.income, settings.turnSettings, settings.chatSettings);
            settings.agents.forEach(config::addAgent);
            var firstTeamRunCMD = createRunCMD(FIRST_TEAM_PATH);
            var secondTeamRunCMD = createRunCMD(SECOND_TEAM_PATH);
            settings.agents.forEach(agent -> {
                var runCommand = agent.getTeam().equals(Team.FIRST) ? firstTeamRunCMD : secondTeamRunCMD;
                var client = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            Process p = Runtime.getRuntime().exec(runCommand + ' ' + agent.getToken());
                            var error = p.getErrorStream();
                            InputStreamReader isrerror = new InputStreamReader(error);
                            BufferedReader bre = new BufferedReader(isrerror);
                            String linee;
                            while ((linee = bre.readLine()) != null) {
                                LOGGER.error("Client error : " + linee);
                            }
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

    private Node findNodeById(List<Node> nodes, int nodeId) {
        return nodes.stream()
                .filter(node -> node.getId() == nodeId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(Node.class.getSimpleName(), nodeId));
    }

    private void addPathToGraph(List<Node> nodes, Graph graph, Path path) {
        graph.addPath(
                path,
                this.findNodeById(nodes, path.getFirstNodeId()),
                this.findNodeById(nodes, path.getSecondNodeId()));
    }

    @Data
    private static class GraphSettings {
        private List<Node> nodes;
        private List<Path> paths;
    }

    @Data
    public static class IncomeSettings {
        private Double policeIncomeEachTurn;
        private Double thiefIncomeEachTurn;

        public HideAndSeek.IncomeSettings toProto() {
            return HideAndSeek.IncomeSettings.newBuilder()
                    .setPoliceIncomeEachTurn(this.policeIncomeEachTurn)
                    .setThievesIncomeEachTurn(this.thiefIncomeEachTurn)
                    .build();
        }
    }

    @Data
    public static class TurnSettings {
        private Integer maxTurns;
        private List<Integer> visibleTurns;

        public HideAndSeek.TurnSettings toProto() {
            return HideAndSeek.TurnSettings.newBuilder()
                    .setMaxTurns(this.maxTurns)
                    .addAllVisibleTurns(this.visibleTurns)
                    .build();
        }
    }

    @Data
    public static class ChatSettings {
        private Integer chatBoxMaxSize;
        private Double chatCostPerCharacter;

        public HideAndSeek.ChatSettings toProto() {
            return HideAndSeek.ChatSettings.newBuilder()
                    .setChatBoxMaxSize(this.chatBoxMaxSize)
                    .setChatCostPerCharacter(this.chatCostPerCharacter)
                    .build();
        }
    }

    @Data
    private static class GameSettings {
        private List<Agent> agents = new ArrayList<>();
        private GraphSettings graph = new GraphSettings();
        private IncomeSettings income = new IncomeSettings();
        private TurnSettings turnSettings = new TurnSettings();
        private ChatSettings chatSettings = new ChatSettings();
    }

    public static void handleCMDArgs(String[] args) {
        for (String arg : args) {
            handleArg(arg);
        }
        try {
            GAME_CONFIG_PATH = args[2];
            MAP_PATH = args[3];
        }catch (Exception ignore){
            LOGGER.error("Invalid args.");
        }

        if ((FIRST_TEAM_PATH == null || SECOND_TEAM_PATH == null)) {
            LOGGER.error("No path for clients");
            System.exit(-1);
        }

        if(GAME_CONFIG_PATH == null){
            LOGGER.error("No path for game config.");
        }

        if(MAP_PATH == null){
            LOGGER.warn("No path for map.json");
        }
    }


    private static void handleArg(String arg) {
        String[] split = arg.split("=");
        try {
            if (split[0].equals("--first-team")) {
                FIRST_TEAM_PATH = split[1];
            } else if (split[0].equals("--second-team")) {
                SECOND_TEAM_PATH = split[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String createRunCMD(String path) {
        path = path.strip();
        if (path.contains(".jar")) {
            return JAVA_EXEC_CMD + " " + path;
        }
        return path;
    }
}
