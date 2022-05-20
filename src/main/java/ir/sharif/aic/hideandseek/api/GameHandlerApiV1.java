package ir.sharif.aic.hideandseek.api;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.GameHandlerGrpc;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.MoveCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class GameHandlerApiV1 extends GameHandlerGrpc.GameHandlerImplBase {
  private final GameService gameService;

  @Override
  public void declareReadiness(
      HideAndSeek.DeclareReadinessCommand cmd, StreamObserver<Empty> responseObserver) {
    this.gameService.handle(new DeclareReadinessCommand(cmd));
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void watch(
      HideAndSeek.WatchCommand cmd, StreamObserver<HideAndSeek.GameView> responseObserver) {

    this.gameService.handle(
        new WatchCommand(
            cmd,
            GrpcEventBroadCaster.builder()
                .agentToken(cmd.getToken())
                .gameService(this.gameService)
                .observer(responseObserver)
                .build()));
  }

  @Override
  public void move(HideAndSeek.MoveCommand cmd, StreamObserver<Empty> responseObserver) {
    this.gameService.handle(new MoveCommand(cmd));
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }
}
