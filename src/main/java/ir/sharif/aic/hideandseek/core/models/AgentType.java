package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum AgentType {
  POLICE(HideAndSeek.AgentType.POLICE),
  THIEF(HideAndSeek.AgentType.THIEF);

  private final HideAndSeek.AgentType proto;

  AgentType(HideAndSeek.AgentType proto) {
    this.proto = proto;
  }

  public HideAndSeek.AgentType toProto() {
    return this.proto;
  }
}
