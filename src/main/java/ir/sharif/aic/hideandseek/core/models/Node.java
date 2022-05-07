package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
  private int id;

  public void validate() {
    GraphValidator.validateNodeId(this.id, "node.id");
  }

  public HideAndSeek.Node toProto() {
    return HideAndSeek.Node.newBuilder().setId(this.id).build();
  }
}
