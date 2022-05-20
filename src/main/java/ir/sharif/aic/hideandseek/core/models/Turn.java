package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Turn {
  private final int turnNumber;
  private final TurnType turnType;

  public Turn next() {
    return new Turn(turnNumber + 1, turnType.next());
  }

  public HideAndSeek.Turn toProto() {
    return HideAndSeek.Turn.newBuilder()
        .setTurnType(this.turnType.toProto())
        .setTurnNumber(this.turnNumber)
        .build();
  }
}
