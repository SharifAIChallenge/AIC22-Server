package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class Path {
  private final int id;
  private final int firstNodeId;
  private final int secondNodeId;
  private final double price;

  public HideAndSeek.Path toProto() {
    return HideAndSeek.Path.newBuilder()
        .setId(this.id)
        .setFirstNodeId(this.firstNodeId)
        .setSecondNodeId(this.secondNodeId)
        .setPrice(this.price)
        .build();
  }
}
