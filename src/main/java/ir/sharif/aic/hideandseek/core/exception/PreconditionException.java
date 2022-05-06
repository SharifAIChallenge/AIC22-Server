package ir.sharif.aic.hideandseek.core.exception;

import io.grpc.Status;

public class PreconditionException extends GameException {
  public PreconditionException(String code) {
    super(code, Status.FAILED_PRECONDITION);
    this.addDetail("code", code);
  }
}
