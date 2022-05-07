package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.AlreadyExistsException;
import ir.sharif.aic.hideandseek.core.exceptions.InternalException;
import ir.sharif.aic.hideandseek.core.exceptions.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Graph {
  private final Map<Integer, Node> nodeMap;
  private final Map<Integer, Path> pathMap;
  private final Map<Node, List<Node>> adjacencyMap;

  public Graph() {
    this.nodeMap = new HashMap<>();
    this.pathMap = new HashMap<>();
    this.adjacencyMap = new HashMap<>();
  }

  public void addNode(Node aNewNode) {
    aNewNode.validate();

    if (this.nodeMap.containsKey(aNewNode.getId())) {
      throw new AlreadyExistsException(aNewNode.getClass().getSimpleName(), aNewNode.getId());
    }

    this.nodeMap.put(aNewNode.getId(), aNewNode);
  }

  public void addPath(Path newPath, Node first, Node second) {
    newPath.validate();
    first.validate();
    second.validate();

    if (this.pathMap.containsKey(newPath.getId()))
      throw new AlreadyExistsException(newPath.getClass().getSimpleName(), newPath.getId());

    this.assertEndNodesMatch(newPath, first, second);
    this.assertContainsNode(first);
    this.assertContainsNode(second);

    // actually adding the map
    this.pathMap.put(newPath.getId(), newPath);

    var firstAdj = this.adjacencyMap.getOrDefault(first, new ArrayList<>());
    firstAdj.add(second);
    this.adjacencyMap.putIfAbsent(first, firstAdj);

    var secondAdj = this.adjacencyMap.getOrDefault(second, new ArrayList<>());
    secondAdj.add(first);
    this.adjacencyMap.putIfAbsent(second, secondAdj);
  }

  public Node getNodeById(int nodeId) {
    this.assertContainsNode(nodeId);
    return this.nodeMap.get(nodeId);
  }

  public Path getPathById(int pathId) {
    this.assertContainsPath(pathId);
    return this.pathMap.get(pathId);
  }

  public boolean isEmpty() {
    return this.nodeMap.isEmpty();
  }

  public HideAndSeek.Graph toProto() {
    return HideAndSeek.Graph.newBuilder()
        .addAllNodes(this.nodeMap.values().stream().map(Node::toProto).collect(Collectors.toList()))
        .addAllPaths(this.pathMap.values().stream().map(Path::toProto).collect(Collectors.toList()))
        .build();
  }

  private void assertEndNodesMatch(Path aPath, Node first, Node second) {
    if (aPath.getFirstNodeId() != first.getId()) {
      throw new InternalException("path's first node id does not math the first node passed");
    }

    if (aPath.getSecondNodeId() != second.getId()) {
      throw new InternalException("path's second node id does not math the second node passed");
    }
  }

  private void assertContainsNode(Node aNode) {
    if (!this.nodeMap.containsKey(aNode.getId()))
      throw new NotFoundException(Node.class.getSimpleName(), aNode.getId());
  }

  private void assertContainsNode(int nodeId) {
    if (!this.nodeMap.containsKey(nodeId))
      throw new NotFoundException(Node.class.getSimpleName(), nodeId);
  }

  private void assertContainsPath(int pathId) {
    if (!this.pathMap.containsKey(pathId))
      throw new NotFoundException(Path.class.getSimpleName(), pathId);
  }
}
