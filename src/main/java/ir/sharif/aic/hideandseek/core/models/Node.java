package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.Data;

@Data
public class Node {
  private int id;

  public void validate() {
    Validator.validateNodeId(this.id, "node.id");
  }

  public HideAndSeek.Node toProto() {
    return HideAndSeek.Node.newBuilder().setId(this.id).build();
  }
}
