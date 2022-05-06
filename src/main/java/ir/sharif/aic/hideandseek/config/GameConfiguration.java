package ir.sharif.aic.hideandseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ir.sharif.aic.hideandseek.core.errors.NotFoundException;
import ir.sharif.aic.hideandseek.core.models.GameSpecs;
import ir.sharif.aic.hideandseek.core.models.Graph;
import ir.sharif.aic.hideandseek.core.models.Node;
import ir.sharif.aic.hideandseek.core.models.Path;
import lombok.Data;
import lombok.Setter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
@Setter
public class GameConfiguration {
  private static final String GAME_CONFIG_PATH = "src/main/resources/game.yml";

  @Data
  private static class TeamSettings {
    private int maxPoliceCount;
    private int maxThiefCount;
  }

  @Data
  private static class GraphSettings {
    private List<Node> nodes;
    private List<Path> paths;
  }

  @Data
  private static class GameSettings {
    private TeamSettings team;
    private GraphSettings graph;
  }

  @Bean
  public GameSpecs createGameSpecs() throws IOException {
    var graph = new Graph();
    var mapper = new ObjectMapper(new YAMLFactory());
    var settings = mapper.readValue(new File(GAME_CONFIG_PATH), GameSettings.class);

    settings.graph.nodes.forEach(graph::addNode);
    settings.graph.paths.forEach(path -> addPathToGraph(settings.graph.nodes, graph, path));

    return new GameSpecs(settings.team.maxThiefCount, settings.team.maxPoliceCount, graph);
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
        findNodeById(nodes, path.getFirstNodeId()),
        this.findNodeById(nodes, path.getSecondNodeId()));
  }
}