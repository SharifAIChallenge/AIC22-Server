package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Node {
  private final int id;

  public HideAndSeek.Node toProto() {
    return HideAndSeek.Node.newBuilder().setId(this.id).build();
  }
}
