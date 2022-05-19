package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.GrpcEventBroadCaster;
import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;
import lombok.Getter;

@Getter
public class WatchCommand {
  private final String token;
  private final GrpcEventBroadCaster watcher;

  public WatchCommand(HideAndSeek.WatchCommand cmd, GrpcEventBroadCaster broadCaster) {
    this.token = cmd.getToken();
    this.watcher = broadCaster;
    this.validate();
  }

  public void validate() {
    TokenValidator.validate(this.token, "watchCommand.token");
  }
}
