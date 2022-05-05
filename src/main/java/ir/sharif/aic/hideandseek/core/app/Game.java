package ir.sharif.aic.hideandseek.core.app;

import ir.sharif.aic.hideandseek.channel.AsyncChannel;
import ir.sharif.aic.hideandseek.channel.Channel;
import ir.sharif.aic.hideandseek.core.event.GameEvent;

public class Game {
  private final Channel<GameEvent> eventChannel;

  public Game() {
    this.eventChannel = new AsyncChannel<>();
  }
}
