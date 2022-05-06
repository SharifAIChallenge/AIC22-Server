package ir.sharif.aic.hideandseek.lib.exceptions;

import io.grpc.Status;

public class PreconditionException extends GameException {
  public PreconditionException(String code) {
    super(code, Status.FAILED_PRECONDITION);
    this.addDetail("code", code);
  }
}
