package ir.sharif.aic.hideandseek.core.exceptions;

import io.grpc.Status;

public class InvalidRequestException extends GameException {

  public InvalidRequestException(String message) {
    super(message, Status.INVALID_ARGUMENT);
  }
}
