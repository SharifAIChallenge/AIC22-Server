package ir.sharif.aic.hideandseek.core.exceptions;

import io.grpc.Status;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public abstract class GameException extends RuntimeException {
  protected final Map<String, String> details;
  private final Date timeStamp;
  private final Status status;

  protected GameException(String message, Status status) {
    super(message);
    this.status = status;
    this.timeStamp = new Date();
    this.details = new HashMap<>();
  }

  public GameException withDetail(String key, Object value) {
    this.details.put(key, value.toString());
    return this;
  }

  protected void addDetail(String key, Object value) {
    this.details.put(key, value.toString());
  }
}
