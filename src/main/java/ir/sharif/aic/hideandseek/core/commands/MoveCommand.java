package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.models.GraphValidator;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;
import lombok.Getter;

@Getter
public class MoveCommand {
  private final String token;
  private final int toNodeId;

  public MoveCommand(HideAndSeek.MoveCommand cmd) {
    this.token = cmd.getToken();
    this.toNodeId = cmd.getToNodeId();
  }

  public void validate() {
    TokenValidator.validate(this.token, "doActionCommand.token");
    GraphValidator.validateNodeId(this.toNodeId, "doActionCommand.toNodeId");
  }
}
