package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.channel.Channel;
import ir.sharif.aic.hideandseek.core.event.GameEvent;
import ir.sharif.aic.hideandseek.core.models.GameSpecs;
import org.springframework.stereotype.Service;

@Service
public class GameService {
  private final GameSpecs specs;
  private final Channel<GameEvent> eventChannel;

  public GameService(GameSpecs specs) {
    this.specs = specs;
    this.eventChannel = new AsyncChannel<>();
  }
}
