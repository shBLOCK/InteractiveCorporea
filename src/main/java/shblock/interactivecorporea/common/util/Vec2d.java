package shblock.interactivecorporea.common.util;

public class Vec2d {
  public double x;
  public double y;

  public Vec2d() {
    this.x = 0F;
    this.y = 0F;
  }

  public Vec2d(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void set(Vec2d vec) {
    x = vec.x;
    y = vec.y;
  }

  public void set(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public void add(Vec2d vec) {
    this.x += vec.x;
    this.y += vec.y;
  }

  public void add(double x, double y) {
    this.x += x;
    this.y += y;
  }

  public void sub(Vec2d vec) {
    this.x -= vec.x;
    this.y -= vec.y;
  }

  public void sub(double x, double y) {
    this.x -= x;
    this.y -= y;
  }

  public double distanceTo(Vec2d vec) {
    double sx = Math.abs(vec.x - x);
    sx *= sx;
    double sy = Math.abs(vec.y - y);
    sy *= sy;
    return Math.sqrt(sx + sy);
  }

  @Override
  public String toString() {
    return String.format("Vec2d(%f, %f)", x, y);
  }
}
