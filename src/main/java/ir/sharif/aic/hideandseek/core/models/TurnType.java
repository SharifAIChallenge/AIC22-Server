package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum TurnType {
    THIEF_TURN(HideAndSeek.TurnType.THIEF_TURN),
    POLICE_TURN(HideAndSeek.TurnType.POLICE_TURN);

    private final HideAndSeek.TurnType proto;

    TurnType(HideAndSeek.TurnType proto) {
        this.proto = proto;
    }

    public TurnType next() {
        return switch (this) {
            case THIEF_TURN -> POLICE_TURN;
            case POLICE_TURN -> THIEF_TURN;
        };
    }

    public HideAndSeek.TurnType toProto() {
        return this.proto;
    }
}
