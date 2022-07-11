package shblock.interactivecorporea.common.util;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.util.math.MathHelper;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;

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

  /**
   * Used to perform a smooth move from A to B, you should call this every tick (at a constant speed).
   * This provides smooth accelerate and decelerate.
   * @param current the current position (distance from A)
   * @param dest the destination position (B)
   * @param prevSpd the last result of this function
   * @param acceleration the acceleration at the start of the animation
   * @param distanceFactor the speed-distance factor, used at the end of the animation
   * @param minSpd the minimum speed
   * @return the result. (remember to store this and pass it to the prevSpd parameter on the next calculation)
   */
  public static double smoothMovingSpeed(double current, double dest, double prevSpd, double acceleration, double distanceFactor, double minSpd) {
    if (Math.abs(dest - current) < minSpd) return dest - current;
    return Math.signum(dest - current) * Math.min(
        Math.abs(dest - current) * distanceFactor + minSpd,
        Math.abs(prevSpd) + acceleration
    );
  }

  public static Vector3 smoothMovingSpeed(Vector3 current, Vector3 dest, Vector3 prevSpd, double acceleration, double distanceFactor, double minSpd) {
    return new Vector3(
        smoothMovingSpeed(current.x, dest.x, prevSpd.x, acceleration, distanceFactor, minSpd),
        smoothMovingSpeed(current.y, dest.y, prevSpd.y, acceleration, distanceFactor, minSpd),
        smoothMovingSpeed(current.z, dest.z, prevSpd.z, acceleration, distanceFactor, minSpd)
    );
  }

  public static Vec2d smoothMovingSpeed(Vec2d current, Vec2d dest, Vec2d prevSpd, double acceleration, double distanceFactor, double minSpd) {
    return new Vec2d(
        smoothMovingSpeed(current.x, dest.x, prevSpd.x, acceleration, distanceFactor, minSpd),
        smoothMovingSpeed(current.y, dest.y, prevSpd.y, acceleration, distanceFactor, minSpd)
    );
  }

  public static Vector3 vec3Divide(Vector3 a, Vector3 b) {
    return new Vector3(a.x / b.x, a.y / b.y, a.z / b.z);
  }

  public static Vector3 apply(Vector3 vec, Double2DoubleFunction func) {
    return new Vector3(
        func.applyAsDouble(vec.x),
        func.applyAsDouble(vec.y),
        func.applyAsDouble(vec.z)
    );
  }

  @Nullable
  public static Vector3 rayPlaneIntersection(Ray3 ray, Ray3 planeNormal) {
    ray.dir = ray.dir.normalize();
    planeNormal.dir = planeNormal.dir.normalize();
    double d = planeNormal.org.subtract(ray.org).dotProduct(planeNormal.dir) / ray.dir.dotProduct(planeNormal.dir);
    if (d < 0)
      return null;
    return ray.org.add(ray.dir.multiply(d));
  }

  public static double pointToLineDistance(Ray3 line, Vector3 point) { //TODO: maybe find a better solution?
    Vector3 orgLine = line.dir.subtract(line.org);
    Vector3 proj = point.project(orgLine);
    return proj.subtract(orgLine).mag();
  }

  public static double pointToOrgLineDistance(Vector3 line, Vector3 point) {
    return point.project(line).subtract(point).mag();
  }
}
