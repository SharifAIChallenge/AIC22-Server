package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.models.Node;
import ir.sharif.aic.hideandseek.core.models.Path;
import ir.sharif.aic.hideandseek.core.models.ProtoMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@AllArgsConstructor
@PropertySource(value = "map.json")
@Getter
public class Graph implements ProtoMapper<HideAndSeek.Graph> {
    private List<Path> paths;
    private List<Node> nodes;

    @Override
    public HideAndSeek.Graph toProto() {
        return null;
    }
}
