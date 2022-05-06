package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.errors.ValidationException;
import lombok.Data;

@Data
public class Path {
  private int id;
  private int firstNodeId;
  private int secondNodeId;
  private double price;

  public void validate() {
    if (this.id <= 0) {
      throw new ValidationException("path id must be positive", "path.id");
    }
    if (this.firstNodeId <= 0) {
      throw new ValidationException("first node id must be positive", "path.firstNodeId");
    }
    if (this.secondNodeId <= 0) {
      throw new ValidationException("second id must be positive", "path.secondNodeId");
    }
    if (this.price < 0) {
      throw new ValidationException("path price cannot be negative", "path.price");
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
