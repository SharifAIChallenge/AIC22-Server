package ir.sharif.aic.hideandseek.handler;


import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeekGameGrpc;
import ir.sharif.aic.hideandseek.service.GameService;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class HideAndSeekGameHandler extends HideAndSeekGameGrpc.HideAndSeekGameImplBase {
    private final GameService gameService;
    @Override
    public void declareReadiness(HideAndSeek.DeclareReadinessRequest request, StreamObserver<HideAndSeek.DeclareReadinessResponse> responseObserver) {
        var response = gameService.declareReadinessForClient(request);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void watch(HideAndSeek.WatchRequest request, StreamObserver<HideAndSeek.GameResponse> responseObserver) {

    }
}
