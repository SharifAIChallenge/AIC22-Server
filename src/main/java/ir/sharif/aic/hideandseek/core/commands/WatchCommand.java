package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;
import lombok.Getter;

@Getter
public class WatchCommand {
  private final String token;

  public WatchCommand(HideAndSeek.WatchCommand cmd) {
    this.token = cmd.getToken();
    this.validate();
  }

  public void validate() {
    TokenValidator.validate(this.token, "watchCommand.token");
  }
}
