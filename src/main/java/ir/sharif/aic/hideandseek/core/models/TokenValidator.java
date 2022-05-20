package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;

public class TokenValidator {
  private TokenValidator() {}

  public static void validate(String token, String target) {
    if (token == null) {
      throw new ValidationException("token cannot be null", target);
    }
    if (token.isBlank()) {
      throw new ValidationException("token cannot be blank", target);
    }
  }
}
