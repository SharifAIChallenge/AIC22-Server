package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum Turn {
  THIEF_TURN,
  POLICE_TURN;

  public HideAndSeek.Turn toProto() {
    switch (this) {
      case THIEF_TURN:
        return HideAndSeek.Turn.THIEF_TURN;
      case POLICE_TURN:
        return HideAndSeek.Turn.POLICE_TURN;
      default:
        throw new IllegalStateException("invalid turn");
    }
  }
}
