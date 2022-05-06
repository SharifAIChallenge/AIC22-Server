package ir.sharif.aic.hideandseek.core.models;

import ir.sharif.aic.hideandseek.core.errors.ValidationException;

public class GraphValidator {
  private GraphValidator() {}

  static void validateNodeId(int value, String target) {
    if (value <= 0) {
      throw new ValidationException("node id must be positive", target);
    }
  }

  static void validatePathId(int value) {
    if (value <= 0) {
      throw new ValidationException("path id must be positive", "path.id");
    }
  }

  static void validatePathPrice(double value) {
    if (value <= 0) {
      throw new ValidationException("path price cannot be negative", "path.price");
    }
  }
}
