package ir.sharif.aic.hideandseek.api;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import ir.sharif.aic.hideandseek.api.grpc.GameHandlerGrpc;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.app.GameService;
import ir.sharif.aic.hideandseek.core.commands.ChatCommand;
import ir.sharif.aic.hideandseek.core.commands.DeclareReadinessCommand;
import ir.sharif.aic.hideandseek.core.commands.MoveCommand;
import ir.sharif.aic.hideandseek.core.commands.WatchCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GameHandlerApiV1 extends GameHandlerGrpc.GameHandlerImplBase {
  private final GameService gameService;

  @Override
  public void declareReadiness(
      HideAndSeek.DeclareReadinessCommand cmd, StreamObserver<Empty> responseObserver) {
    log.info("Received readiness command from client {}", cmd.getToken());
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
    log.info("Received move command from client {} to move to node {}", cmd.getToken(), cmd.getToNodeId());

    this.gameService.handle(new MoveCommand(cmd));
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public void sendMessage(HideAndSeek.ChatCommand cmd, StreamObserver<Empty> responseObserver) {
    log.info("Received sendMessage command from client {} with text = '{}'", cmd.getToken(), cmd.getText());

    this.gameService.handle(new ChatCommand(cmd));
    responseObserver.onNext(Empty.getDefaultInstance());
    responseObserver.onCompleted();
  }
}
