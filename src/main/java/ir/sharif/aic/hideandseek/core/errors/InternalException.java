package ir.sharif.aic.hideandseek.core.errors;

import io.grpc.Status;

public class InternalException extends GameException {
  public InternalException(String message) {
    super(message, Status.INTERNAL);
  }
}
