package ir.sharif.aic.hideandseek.core.exceptions;

import io.grpc.Status;

public class InternalException extends GameException {
  public InternalException(String message) {
    super(message, Status.INTERNAL);
  }

  @Override
  public InternalException withDetail(String key, Object value) {
    this.details.put(key, value.toString());
    return this;
  }
}
