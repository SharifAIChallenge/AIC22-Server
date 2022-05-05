package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.channel.Channel;
import ir.sharif.aic.hideandseek.core.event.GameEvent;
import org.springframework.stereotype.Service;

@Service
public class GameService {
  private final Channel<GameEvent> eventChannel;

  public GameService() {
    this.eventChannel = new AsyncChannel<>();
  }
}
