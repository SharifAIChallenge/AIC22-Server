package ir.sharif.aic.hideandseek.core.commands;

import ir.sharif.aic.hideandseek.api.grpc.HideAndSeek;
import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;
import ir.sharif.aic.hideandseek.core.models.TokenValidator;
import lombok.Getter;

import java.util.regex.Pattern;

@Getter
public class ChatCommand {
  private static final Pattern CHAT_MESSAGE_PATTERN = Pattern.compile("^[01]*$");

  private final String token;
  private final String text;

  public ChatCommand(HideAndSeek.ChatCommand cmd) {
    this.token = cmd.getToken();
    this.text = cmd.getText();
  }

  public void validate() {
    TokenValidator.validate(this.token, "chatCommand.token");

    if (!CHAT_MESSAGE_PATTERN.matcher(this.text).matches()) {
      throw new ValidationException("chat text must only include 0 and 1", "chatCommand.text");
    }
  }
}
