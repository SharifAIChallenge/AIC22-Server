package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.events.GameEvent;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;
import ir.sharif.aic.hideandseek.lib.channel.Watcher;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WatchCommand {
  private final String token;
  private final Watcher<GameEvent> watcher;

  public WatchCommand(HideAndSeek.WatchCommand cmd, Watcher<GameEvent> watcher) {
    this.token = cmd.getToken();
    this.watcher = watcher;
    this.validate();
  }

  public void validate() {
    TokenValidator.validate(this.token, "watchCommand.token");
  }
}
