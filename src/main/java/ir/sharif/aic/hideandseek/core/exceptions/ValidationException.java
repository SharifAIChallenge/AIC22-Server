package ir.sharif.aic.hideandseek.core.exceptions;

import io.grpc.Status;

import java.util.List;

public class ValidationException extends GameException {
  public ValidationException(String message, List<String> targetFields) {
    super(message, Status.INVALID_ARGUMENT);
    this.addDetail("fields", targetFields);
  }

  public ValidationException(String message, String targetField) {
    this(message, List.of(targetField));
  }
}
