package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.Data;

@Data
public class Path {
  private int id;
  private int firstNodeId;
  private int secondNodeId;
  private double price;

  public void validate() {
    GraphValidator.validatePathId(this.id);
    GraphValidator.validateNodeId(this.firstNodeId, "path.firstNodeId");
    GraphValidator.validateNodeId(this.secondNodeId, "path.secondNodeId");
    GraphValidator.validatePathPrice(this.price);
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
