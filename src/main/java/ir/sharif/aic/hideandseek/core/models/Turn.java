package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum Turn {
    THIEF_TURN(HideAndSeek.Turn.THIEF_TURN),
    POLICE_TURN(HideAndSeek.Turn.POLICE_TURN);

    private final HideAndSeek.Turn proto;

    Turn(HideAndSeek.Turn proto) {
        this.proto = proto;
    }

    public Turn next() {
        return switch (this) {
            case THIEF_TURN -> POLICE_TURN;
            case POLICE_TURN -> THIEF_TURN;
        };
    }

    public HideAndSeek.Turn toProto() {
        return this.proto;
    }
}
