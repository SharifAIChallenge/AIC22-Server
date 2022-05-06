package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.core.errors.AlreadyExistsException;

import java.util.HashMap;
import java.util.Map;

public class Graph {
  private Map<Integer, Node> nodeMap;
  private Map<Integer, Path> pathMap;

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
}
