package ir.sharif.aic.hideandseek.api;

import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.GameHandlerGrpc;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;

@GRpcService
@RequiredArgsConstructor
public class GameHandlerApiV1 extends GameHandlerGrpc.GameHandlerImplBase {
  private final GameService gameService;

  @Override
  public void declareReadiness(
      HideAndSeek.DeclareReadinessCommand cmd,
      StreamObserver<HideAndSeek.DeclareReadinessReply> responseObserver) {
    super.declareReadiness(cmd, responseObserver);
  }

  @Override
  public void watch(
      HideAndSeek.WatchCommand request, StreamObserver<HideAndSeek.GameView> responseObserver) {
    super.watch(request, responseObserver);
  }

  @Override
  public void doAction(
      HideAndSeek.DoActionCommand request,
      StreamObserver<HideAndSeek.DoActionReply> responseObserver) {
    super.doAction(request, responseObserver);
  }
}
