package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;

public class DeclareReadinessCommand {
  private Integer startNodeId;
  private String token;

  public DeclareReadinessCommand(HideAndSeek.DeclareReadinessCommand cmd) {
    this.startNodeId = cmd.getStartNodeId();
    this.token = cmd.getToken();
    this.validate();
  }

  public void validate() {
    TokenValidator.validate(this.token, "declareReadinessCommand.token");
  }
}
