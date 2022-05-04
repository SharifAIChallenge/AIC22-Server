package ir.sharif.aic.hideandseek.database;

import ir.sharif.aic.hideandseek.models.Node;
import ir.sharif.aic.hideandseek.models.Player;
import ir.sharif.aic.hideandseek.models.Vector;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class InMemoryDataBase {

    @Builder.Default
    private List<Player> players = new ArrayList<>();
    private List<Node> nodes;
    private List<Vector> vectors;

}
