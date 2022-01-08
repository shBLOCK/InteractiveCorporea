package shblock.interactivecorporea.client.util;

import net.minecraft.util.math.MathHelper;

public class MathUtil {
  /**
   * Calculates the z-offset needed to avoid a flat surface rendered in front of the halo being partially behind the halo
   */
  public static double calcChordCenterDistance(double radius, double chord) {
    return MathHelper.sqrt(radius * radius - (chord / 2F) * (chord / 2F));
  }

  public static double calcRadiansFromChord(double radius, double chord) {
    return Math.asin(chord / 2D / radius) * 2D;
  }
}
