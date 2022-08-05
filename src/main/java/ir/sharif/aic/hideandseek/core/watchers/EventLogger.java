package ir.sharif.aic.hideandseek.core.watchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.sharif.aic.hideandseek.config.GraphicLogger;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;

import java.util.logging.Logger;

public class EventLogger implements Watcher<GameEvent> {
  private final Logger logger;
  private final ObjectMapper serializer;

  public EventLogger(ObjectMapper serializer) {
    this.logger = Logger.getLogger(EventLogger.class.getName());
    this.serializer = serializer;
  }

  @Override
  public void watch(GameEvent msg) {
    String serialized = "";
    try {
      serialized = this.serializer.writeValueAsString(msg);
    } catch (JsonProcessingException ignored) {
      // there will never be a serialization error
    }
    this.logger.info(serialized);
    GraphicLogger.getInstance().appendLog(serialized);
  }
}
