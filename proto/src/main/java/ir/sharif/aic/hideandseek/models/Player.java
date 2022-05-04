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
    private String token;
    private final Integer id;
    // private final int nodeId;
    private final PlayerType playerType;
    private final Team team;
    private boolean isAlive;


}
