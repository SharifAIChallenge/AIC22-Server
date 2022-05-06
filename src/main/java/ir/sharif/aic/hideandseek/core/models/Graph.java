package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.errors.AlreadyExistsException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Graph {
  private final Map<Integer, Node> nodeMap;
  private final Map<Integer, Path> pathMap;

  public Graph() {
    this.nodeMap = new HashMap<>();
    this.pathMap = new HashMap<>();
  }

  public void addNode(Node aNewNode) {
    if (this.nodeMap.containsKey(aNewNode.getId())) {
      throw new AlreadyExistsException(aNewNode.getClass().getSimpleName(), aNewNode.getId());
    }

    this.nodeMap.put(aNewNode.getId(), aNewNode);
  }

  public HideAndSeek.Graph toProto() {
    return HideAndSeek.Graph.newBuilder()
        .addAllNodes(this.nodeMap.values().stream().map(Node::toProto).collect(Collectors.toList()))
        .addAllPaths(this.pathMap.values().stream().map(Path::toProto).collect(Collectors.toList()))
        .build();
  }
}
