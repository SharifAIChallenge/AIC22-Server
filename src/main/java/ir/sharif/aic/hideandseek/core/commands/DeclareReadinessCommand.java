package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;
import lombok.Getter;

@Getter
public class DeclareReadinessCommand {
  private final int startNodeId;
  private final String token;

  public DeclareReadinessCommand(HideAndSeek.DeclareReadinessCommand cmd) {
    this.startNodeId = cmd.getStartNodeId();
    this.token = cmd.getToken();
    this.validate();
  }

  public void validate() {
    TokenValidator.validate(this.token, "declareReadinessCommand.token");
  }
}
