package ir.sharif.aic.hideandseek.core.exceptions;

import io.grpc.Status;

import java.util.Map;

public class NotFoundException extends GameException {
  public NotFoundException(String resource, Object id) {
    super(
        String.format("resource %s with id: %s was not found", resource, id.toString()),
        Status.NOT_FOUND);
    this.addDetail("resource", resource);
    this.addDetail("id", id);
  }

  public NotFoundException(String resource, Map<String, String> criteria) {
    super(
        String.format("resource %s with criteria: %s was not found", resource, criteria.toString()),
        Status.NOT_FOUND);
    this.addDetail("resource", resource);
    this.addDetail("criteria", criteria.toString());
  }
}
