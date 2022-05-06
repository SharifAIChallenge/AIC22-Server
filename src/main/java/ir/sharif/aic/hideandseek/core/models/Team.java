package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum Team {
  FIRST,
  SECOND;

  public HideAndSeek.Team toProto() {
    switch (this) {
      case FIRST:
        return HideAndSeek.Team.FIRST;
      case SECOND:
        return HideAndSeek.Team.SECOND;
      default:
        throw new IllegalArgumentException("invalid team type");
    }
  }

  public Team otherTeam() {
    switch (this) {
      case FIRST:
        return SECOND;
      case SECOND:
        return FIRST;
      default:
        throw new IllegalArgumentException("invalid team type");
    }
  }
}
