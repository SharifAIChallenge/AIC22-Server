package ir.sharif.aic.hideandseek.database;

import ir.sharif.aic.hideandseek.models.Node;
import ir.sharif.aic.hideandseek.models.Player;
import ir.sharif.aic.hideandseek.models.Turn;
import ir.sharif.aic.hideandseek.models.Vector;
import lombok.Builder;
import lombok.Data;
import org.modelmapper.ModelMapper;

import java.util.List;

@Data
@Builder
public class InMemoryDataBase {
    @Builder.Default
    private final ModelMapper modelMapper = new ModelMapper();
    private Turn turn;
    private List<Player> players;
    private List<Node> nodes;
    private List<Vector> vectors;

}
