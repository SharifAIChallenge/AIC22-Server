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
    Validator.validatePathId(this.id);
    Validator.validateNodeId(this.firstNodeId, "path.firstNodeId");
    Validator.validateNodeId(this.secondNodeId, "path.secondNodeId");
    Validator.validatePathPrice(this.price);
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
