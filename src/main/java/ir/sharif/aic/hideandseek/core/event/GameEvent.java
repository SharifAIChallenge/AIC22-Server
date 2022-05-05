package ir.sharif.aic.hideandseek.core.event;

import lombok.Getter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public abstract class GameEvent {
  protected final String id;
  protected final Date timeStamp;
  protected String message;
  protected Map<String, Object> context;
  protected final GameEventType type;

  protected GameEvent(GameEventType type) {
    this.id = UUID.randomUUID().toString();
    this.message = "";
    this.timeStamp = new Date();
    this.context = new HashMap<>();
    this.type = type;
  }
}
