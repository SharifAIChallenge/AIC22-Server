package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.core.exceptions.ValidationException;

public class GraphValidator {
  private GraphValidator() {}

  public static void validateNodeId(int value, String target) {
    if (value <= 0) {
      throw new ValidationException("node id must be positive", target);
    }
  }

  public static void validatePathId(int value) {
    if (value <= 0) {
      throw new ValidationException("path id must be positive", "path.id");
    }
  }

  public static void validatePathPrice(double value) {
    if (value < 0) {
      throw new ValidationException("path price cannot be negative", "path.price");
    }
  }
}
