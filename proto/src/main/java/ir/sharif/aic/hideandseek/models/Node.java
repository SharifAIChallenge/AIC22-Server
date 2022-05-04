package ir.sharif.aic.hideandseek.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Node implements Cloneable{
    private Integer nodeId;
    private List<Vector> vectors;
    private List<Player> players;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
