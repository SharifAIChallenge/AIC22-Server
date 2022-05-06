package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum GameResult {
  UNKNOWN,
  FIRST_WINS,
  SECOND_WINS,
  TIE;

  public HideAndSeek.GameResult toProto() {
    switch (this) {
      case UNKNOWN:
        return HideAndSeek.GameResult.UNKNOWN;
      case FIRST_WINS:
        return HideAndSeek.GameResult.FIRST_WINS;
      case SECOND_WINS:
        return HideAndSeek.GameResult.SECOND_WINS;
      case TIE:
        return HideAndSeek.GameResult.TIE;
      default:
        throw new IllegalStateException("invalid game result");
    }
  }
}
