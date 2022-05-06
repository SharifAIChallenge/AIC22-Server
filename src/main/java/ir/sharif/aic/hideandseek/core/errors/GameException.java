package ir.sharif.aic.hideandseek.core.errors;

import io.grpc.Status;
import lombok.Getter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class GameException extends RuntimeException {
  private final Date timeStamp;
  private final Map<String, String> details;
  private final Status status;

  protected GameException(String message, Status status) {
    super(message);
    this.status = status;
    this.timeStamp = new Date();
    this.details = new HashMap<>();
  }

  protected void addDetail(String key, Object value) {
    this.details.put(key, value.toString());
  }
}
