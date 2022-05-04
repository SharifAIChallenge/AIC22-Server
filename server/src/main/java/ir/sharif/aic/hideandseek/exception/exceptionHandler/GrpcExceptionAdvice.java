package ir.sharif.aic.hideandseek.exception.exceptionHandler;

import io.grpc.Status;
import ir.sharif.aic.hideandseek.exception.DeclareReadinessException;
import ir.sharif.aic.hideandseek.exception.InvalidTokenIdException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {
    @GrpcExceptionHandler
    public Status handleInvalidReadinessRequest(DeclareReadinessException e){
        return Status.OUT_OF_RANGE.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler
    public Status handleInvalidTokenId(InvalidTokenIdException e){
        return Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e);
    }
}
