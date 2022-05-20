package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Path {
  private int id;
  private int firstNodeId;
  private int secondNodeId;
  private double price;

  public boolean isBetween(int nodeId, int anotherNodeId) {
    return this.firstNodeId == nodeId && this.secondNodeId == anotherNodeId
        || this.secondNodeId == nodeId && this.firstNodeId == anotherNodeId;
  }

  public void validate() {
    GraphValidator.validatePathId(this.id);
    GraphValidator.validateNodeId(this.firstNodeId, "path.firstNodeId");
    GraphValidator.validateNodeId(this.secondNodeId, "path.secondNodeId");
    GraphValidator.validatePathPrice(this.price);
    if (this.firstNodeId == this.secondNodeId) {
      throw new ValidationException(
          "a node cannot have a path to itself", List.of("path.firstNodeId", "path.secondNodeId"));
    }
  }

  public HideAndSeek.Path toProto() {
    return HideAndSeek.Path.newBuilder()
        .setId(this.id)
        .setFirstNodeId(this.firstNodeId)
        .setSecondNodeId(this.secondNodeId)
        .setPrice(this.price)
        .build();
  }
}
