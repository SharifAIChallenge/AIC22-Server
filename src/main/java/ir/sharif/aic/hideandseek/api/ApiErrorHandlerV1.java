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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@GrpcAdvice
@RequiredArgsConstructor
@Slf4j
public class ApiErrorHandlerV1 {
  private static final String API_ERROR_DOMAIN = "aic22.sharif.edu";
  private static Logger detailLogger = LoggerFactory.getLogger("analytics");

  @GrpcExceptionHandler
  public StatusException handleGameException(GameException exc) {
    log.error(exc.toString(), exc);

    try {
      detailLogger.error("GameException occurred", exc);
    } catch (SecurityException e) {
      e.printStackTrace();
    }

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
    log.error(exc.toString(), exc);

    var msg = "an unknown error occurred";
    var metadata = new Metadata();
    var info = ErrorInfo.newBuilder().setReason(msg).setDomain(API_ERROR_DOMAIN).build();
    metadata.put(ProtoUtils.keyForProto(info), info);

    return Status.INTERNAL.withDescription(msg).asException();
  }
}
