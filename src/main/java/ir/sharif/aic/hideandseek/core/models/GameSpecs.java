package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class GameSpecs implements ProtoMapper<HideAndSeek.GameSpecs> {
    private final Integer max_thief_count;
    private final Integer max_police_count;
    private final Graph graph_map;

    @Override
    public HideAndSeek.GameSpecs toProto() {
        return null;
    }
}
