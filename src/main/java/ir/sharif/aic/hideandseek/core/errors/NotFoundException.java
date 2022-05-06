package ir.sharif.aic.hideandseek.core.errors;

import io.grpc.Status;

public class NotFoundException extends GameException {
  public NotFoundException(String resource, Object id) {
    super(
        String.format("resource %s with id: %s was not found", resource, id.toString()),
        Status.NOT_FOUND);
    this.addDetail("resource", resource);
    this.addDetail("id", id);
  }
}
