package ir.sharif.aic.hideandseek.api;

import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.GameHandlerGrpc;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class GameHandlerApiV1 extends GameHandlerGrpc.GameHandlerImplBase {
  private final GameService gameService;

  @Override
  public void declareReadiness(
      HideAndSeek.DeclareReadinessCommand cmd,
      StreamObserver<HideAndSeek.DeclareReadinessReply> responseObserver) {
    this.gameService.handle(new DeclareReadinessCommand(cmd));
    responseObserver.onNext(HideAndSeek.DeclareReadinessReply.newBuilder().build());
    responseObserver.onCompleted();
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
