package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum GameStatus implements ProtoMapper<HideAndSeek.GameStatus> {
  PENDING,
  ONGOING,
  FINISHED;

  public GameStatus next() {
    switch (this) {
      case PENDING:
        return ONGOING;
      case ONGOING:
        return FINISHED;
      default:
        throw new IllegalStateException("there is no next game status");
    }
  }

  @Override
  public HideAndSeek.GameStatus toProto() {
    switch (this) {
      case PENDING:
        return HideAndSeek.GameStatus.PENDING;
      case ONGOING:
        return HideAndSeek.GameStatus.ONGOING;
      case FINISHED:
        return HideAndSeek.GameStatus.FINISHED;
      default:
        throw new IllegalStateException("invalid game status");
    }
  }
}
