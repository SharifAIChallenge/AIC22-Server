package ir.sharif.aic.hideandseek.handlers;

import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeekGameGrpc;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
public class HideAndSeekGameHandler extends HideAndSeekGameGrpc.HideAndSeekGameImplBase {
  @Override
  public void healthCheck(HideAndSeek.Ping request, StreamObserver<HideAndSeek.Pong> responseObserver) {
    var response = HideAndSeek.Pong.newBuilder().build();
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

  @Override
  public void declareReadiness(
      HideAndSeek.DeclareReadinessRequest request,
      StreamObserver<HideAndSeek.DeclareReadinessResponse> responseObserver) {
  }
}
