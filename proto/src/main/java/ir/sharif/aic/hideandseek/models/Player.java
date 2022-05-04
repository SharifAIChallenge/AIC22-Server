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


}
