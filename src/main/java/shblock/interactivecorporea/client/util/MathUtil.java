package shblock.interactivecorporea.client.util;

import net.minecraft.util.ColorHelper;
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

  public static float[] hsvToRGB(float hue, float saturation, float value) {
    int i = (int)(hue * 6.0F) % 6;
    float f = hue * 6.0F - (float)i;
    float f1 = value * (1.0F - saturation);
    float f2 = value * (1.0F - f * saturation);
    float f3 = value * (1.0F - (1.0F - f) * saturation);
    float r;
    float g;
    float b;
    switch(i) {
      case 0:
        r = value;
        g = f3;
        b = f1;
        break;
      case 1:
        r = f2;
        g = value;
        b = f1;
        break;
      case 2:
        r = f1;
        g = value;
        b = f3;
        break;
      case 3:
        r = f1;
        g = f2;
        b = value;
        break;
      case 4:
        r = f3;
        g = f1;
        b = value;
        break;
      case 5:
        r = value;
        g = f1;
        b = f2;
        break;
      default:
        throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
    }

    return new float[]{r, g, b};
  }

  public static float[] hsvaToRGBA(float hue, float saturation, float value, float alpha) {
    float[] rgb = hsvToRGB(hue, saturation, value);
    return new float[]{rgb[0], rgb[1], rgb[2], alpha};
  }

  public static int colorToInt(float r, float g, float b) {
    int rr = (int) (r * 255);
    int gg = (int) (g * 255);
    int bb = (int) (b * 255);
    return rr << 16 | gg << 8 | bb;
  }

  public static int colorToInt(float r, float g, float b, float a) {
    int aa = (int) (a * 255);
    return aa << 24 | colorToInt(r, g, b);
  }

  public static float[] revertColor(float[] color) {
    assert color.length == 3 || color.length == 4;
    int len = color.length;
    float[] outColor = new float[len];
    for (int i = 0; i < len; i++) {
      if (i != 3) {
        outColor[i] = 1 - color[i];
      } else {
        outColor[i] = color[i];
      }
    }
    return outColor;
  }
}
