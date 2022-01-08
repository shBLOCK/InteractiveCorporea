package shblock.interactivecorporea.common.util;

import java.text.DecimalFormat;

public class TextHelper {
  private static final DecimalFormat FORMATTER = new DecimalFormat();
  static {
    FORMATTER.applyPattern("0,000");
  }

  public static String formatBigNumber(int amount, boolean compact) {
    if (!compact) {
      return FORMATTER.format(amount);
    }
    if (amount < 1E3) {
      return Integer.toString(amount);
    }
    if (amount < 1E6) {
      return String.format("%.1fK", amount / 1E3F);
    }
    return String.format("%.1fM", amount / 1E6F);
  }
}
