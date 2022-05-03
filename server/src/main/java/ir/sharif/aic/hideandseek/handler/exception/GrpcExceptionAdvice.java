package ir.sharif.aic.hideandseek.handler.exception;

import io.grpc.Status;
import ir.sharif.aic.hideandseek.exception.DeclareReadinessException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {
    @GrpcExceptionHandler
    public Status handleInvalidReadinessRequest(DeclareReadinessException e){
        return Status.OUT_OF_RANGE.withDescription(e.getMessage()).withCause(e);
    }
}
