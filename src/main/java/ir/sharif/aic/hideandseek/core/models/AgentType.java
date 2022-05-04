package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum AgentType {
  POLICE,
  THIEF;

  public AgentType otherType() {
    switch (this) {
      case POLICE:
        return THIEF;
      case THIEF:
        return POLICE;
      default:
        throw new IllegalStateException("invalid agent type");
    }
  }

  public HideAndSeek.AgentType toProto() {
    switch (this) {
      case POLICE:
        return HideAndSeek.AgentType.POLICE;
      case THIEF:
        return HideAndSeek.AgentType.THIEF;
      default:
        throw new IllegalStateException("invalid agent type");
    }
  }
}
