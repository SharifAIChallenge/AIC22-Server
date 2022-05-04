package ir.sharif.aic.hideandseek.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Vector {
    private  Integer vectorId;
    private  Integer firstNodeId;
    private  Integer secondNodeId;
    private  Float price;
}
