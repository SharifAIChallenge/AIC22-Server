package ir.sharif.aic.hideandseek.api;

import com.google.rpc.ErrorInfo;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.protobuf.ProtoUtils;
import ir.sharif.aic.hideandseek.core.exceptions.GameException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiErrorHandlerV1 {
  private static final String API_ERROR_DOMAIN = "aic22.sharif.edu";

  @GrpcExceptionHandler
  public StatusException handleGameException(GameException exc) {
    log.error(exc.toString());

    var metadata = new Metadata();
    var info =
        ErrorInfo.newBuilder()
            .setReason(exc.getMessage())
            .setDomain(API_ERROR_DOMAIN)
            .putAllMetadata(exc.getDetails())
            .build();
    metadata.put(ProtoUtils.keyForProto(info), info);

    return exc.getStatus().withDescription(exc.getMessage()).withCause(exc).asException();
  }

  @GrpcExceptionHandler
  public StatusException handleUnknownRuntimeException(RuntimeException exc) {
    log.error(exc.toString());

    var msg = "an unknown error occurred";
    var metadata = new Metadata();
    var info = ErrorInfo.newBuilder().setReason(msg).setDomain(API_ERROR_DOMAIN).build();
    metadata.put(ProtoUtils.keyForProto(info), info);

    return Status.INTERNAL.withDescription(msg).asException();
  }
}
