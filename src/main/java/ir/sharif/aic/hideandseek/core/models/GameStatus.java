package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum GameStatus {
  PENDING,
  ONGOING,
  FINISHED;

  public HideAndSeek.GameStatus toProto() {
    return switch (this) {
      case PENDING -> HideAndSeek.GameStatus.PENDING;
      case ONGOING -> HideAndSeek.GameStatus.ONGOING;
      case FINISHED -> HideAndSeek.GameStatus.FINISHED;
    };
  }
}
