package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;

public enum GameResult {
    UNKNOWN,
    FIRST_WINS,
    SECOND_WINS;

    public HideAndSeek.GameResult toProto() {
        return switch (this) {
            case UNKNOWN -> HideAndSeek.GameResult.UNKNOWN;
            case FIRST_WINS -> HideAndSeek.GameResult.FIRST_WINS;
            case SECOND_WINS -> HideAndSeek.GameResult.SECOND_WINS;
        };
    }
}
