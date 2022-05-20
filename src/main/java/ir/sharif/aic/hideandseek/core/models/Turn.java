package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Turn {
  private int turnNumber;
  private TurnType turnType;

  public void next() {
    turnType = turnType.next();
    turnNumber++;
  }

  public HideAndSeek.Turn toProto() {
    return HideAndSeek.Turn.newBuilder()
        .setTurnType(this.turnType.toProto())
        .setTurnNumber(this.turnNumber)
        .build();
  }
}
