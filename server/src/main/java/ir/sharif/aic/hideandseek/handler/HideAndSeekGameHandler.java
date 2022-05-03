package ir.sharif.aic.hideandseek.handler;


import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeekGameGrpc;
import ir.sharif.aic.hideandseek.service.GameService;
import ir.sharif.aic.hideandseek.util.Utility;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class HideAndSeekGameHandler extends HideAndSeekGameGrpc.HideAndSeekGameImplBase {
    private final GameService gameService;
    @Override
    public void declareReadiness(HideAndSeek.DeclareReadinessRequest request, StreamObserver<HideAndSeek.DeclareReadinessResponse> responseObserver) {
        String player_token = Utility.generateNewToken();
        var response = gameService.declareReadinessForClient(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
