package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class GameSpecs {
    private final Integer maxThiefCount;
    private final Integer maxPoliceCount;
    private final Graph graphMap;

    public HideAndSeek.GameSpecs toProto() {
        return null;
    }
}
