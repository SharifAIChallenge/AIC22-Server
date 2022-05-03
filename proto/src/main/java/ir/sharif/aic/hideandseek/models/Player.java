package ir.sharif.aic.hideandseek.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Player {
    private final String token;
    // private final int nodeId;
    private final HideAndSeek.PlayerType playerType;
    private final HideAndSeek.Team team;
    private boolean isAlive;

    public Player(String token, HideAndSeek.PlayerType playerType, HideAndSeek.Team team) {
        this.token = token;
        this.playerType = playerType;
        this.team = team;
        this.isAlive = true;
    }


}
