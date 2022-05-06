package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.errors.ValidationException;
import lombok.Data;

@Data
public class Node {
  private int id;

  public void validate() {
    if (this.id <= 0) {
      throw new ValidationException("node id must be positive", "node.id");
    }
  }

  public HideAndSeek.Node toProto() {
    return HideAndSeek.Node.newBuilder().setId(this.id).build();
  }
}
