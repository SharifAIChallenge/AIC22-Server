package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private int id;
    private List<Node> visibleRadiusXPoliceThief = new ArrayList<>();
    private List<Node> visibleRadiusYPoliceJoker = new ArrayList<>();
    private List<Node> visibleRadiusZThiefBatman = new ArrayList<>();

    public void validate() {
        GraphValidator.validateNodeId(this.id, "node.id");
    }

    public HideAndSeek.Node toProto() {
        return HideAndSeek.Node.newBuilder().setId(this.id).build();
    }
}
