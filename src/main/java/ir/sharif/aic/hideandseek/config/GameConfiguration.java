package ir.sharif.aic.hideandseek.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;
import ir.sharif.aic.hideandseek.core.models.*;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Configuration
@Setter
@Slf4j
public class GameConfiguration {
  private static final String GAME_CONFIG_PATH = "src/main/resources/game.yml";

  @Bean
  public GameSpecs createGameSpecs() throws IOException {
    var graph = new Graph();
    var mapper = new ObjectMapper(new YAMLFactory());

    try {
      var settings = mapper.readValue(new File(GAME_CONFIG_PATH), GameSettings.class);

      settings.graph.nodes.forEach(graph::addNode);
      settings.graph.paths.forEach(path -> addPathToGraph(settings.graph.nodes, graph, path));

      var specs =
          GameSpecs.builder()
              .maxPoliceCount(settings.team.maxPoliceCount)
              .maxThiefCount(settings.team.maxThiefCount)
              .graphMap(graph)
              .build();

      settings.team.agents.forEach(specs::addAgent);
      return specs;

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
        findNodeById(nodes, path.getFirstNodeId()),
        this.findNodeById(nodes, path.getSecondNodeId()));
  }

  @Data
  private static class TeamSettings {
    private int maxPoliceCount;
    private int maxThiefCount;
    private List<Agent> agents;
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
}
