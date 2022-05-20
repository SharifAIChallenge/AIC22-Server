package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum Team {
  FIRST,
  SECOND;

  public HideAndSeek.Team toProto() {
    return switch (this) {
      case FIRST -> HideAndSeek.Team.FIRST;
      case SECOND -> HideAndSeek.Team.SECOND;
    };
  }

  public Team otherTeam() {
    return switch (this) {
      case FIRST -> SECOND;
      case SECOND -> FIRST;
    };
  }
}
