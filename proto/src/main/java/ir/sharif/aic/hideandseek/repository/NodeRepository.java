package ir.sharif.aic.hideandseek.repository;

import ir.sharif.aic.hideandseek.database.InMemoryDataBase;
import ir.sharif.aic.hideandseek.models.Node;
import ir.sharif.aic.hideandseek.models.Player;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class NodeRepository {
    private final InMemoryDataBase database;
    private final PlayerRepository playerRepository;

    public List<Node> getAllVisibleNodeForPlayer(Player player){

    }

    public List<Node> getAllNodes(){

    }

    public Node createNodeWithoutPlayersToken(Node node){

    }

}
