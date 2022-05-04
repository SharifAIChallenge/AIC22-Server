package ir.sharif.aic.hideandseek.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Node {
    private Integer nodeId;
    private List<Vector> vectors;
    private List<Integer> playersIds;
}
