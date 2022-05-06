package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
public class GameSpecs {
  private final int maxThiefCount;
  private final int maxPoliceCount;
  private final Graph graphMap;

  public HideAndSeek.GameSpecs toProto() {
    return HideAndSeek.GameSpecs.newBuilder()
        .setMaxPoliceCount(this.maxPoliceCount)
        .setMaxThiefCount(this.maxThiefCount)
        .setGraphMap(graphMap.toProto())
        .build();
  }
}
