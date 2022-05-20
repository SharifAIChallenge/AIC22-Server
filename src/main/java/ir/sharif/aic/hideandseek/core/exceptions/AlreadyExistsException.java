package ir.sharif.aic.hideandseek.core.exceptions;

import io.grpc.Status;

public class AlreadyExistsException extends GameException {
  public AlreadyExistsException(String resource, Object id) {
    super(
        String.format("resource %s with id: %s already exists", resource, id.toString()),
        Status.ALREADY_EXISTS);
    this.addDetail("resource", resource);
    this.addDetail("id", id);
  }
}
