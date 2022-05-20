package ir.sharif.aic.hideandseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.models.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
@Slf4j
public class GameConfigInjector {
    private static final String GAME_CONFIG_PATH = "src/main/resources/game.yml";

    @Bean
    public GameConfig createGameRepository() throws IOException {
        var graph = new Graph();
        var mapper = new ObjectMapper(new YAMLFactory());

        try {
            var settings = mapper.readValue(new File(GAME_CONFIG_PATH), GameSettings.class);

            settings.graph.nodes.forEach(graph::addNode);
            settings.graph.paths.forEach(path -> addPathToGraph(settings.graph.nodes, graph, path));

            var repository = new GameConfig(graph, settings.income);
            settings.agents.forEach(repository::addAgent);

            return repository;

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
    private static class GameSettings {
        private List<Agent> agents;
        private GraphSettings graph;
        private IncomeSettings income;
    }
}
