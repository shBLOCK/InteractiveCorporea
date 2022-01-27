package shblock.interactivecorporea.common.util;

public class Vec3d {
  public double x;
  public double y;
  public double z;

  public Vec3d() {}

  public Vec3d(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Vec3d set(Vec3d vec) {
    x = vec.x;
    y = vec.y;
    z = vec.z;
    return this;
  }

  public Vec3d set(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    return this;
  }

  public Vec3d add(Vec3d vec) {
    this.x += vec.x;
    this.y += vec.y;
    this.z += vec.z;
    return this;
  }

  public Vec3d add(double x, double y, double z) {
    this.x += x;
    this.y += y;
    this.z += z;
    return this;
  }

  public Vec3d sub(Vec3d vec) {
    this.x -= vec.x;
    this.y -= vec.y;
    this.z -= vec.z;
    return this;
  }

  public Vec3d sub(double x, double y, double z) {
    this.x -= x;
    this.y -= y;
    this.z -= z;
    return this;
  }

  public Vec3d mul(double a) {
    this.x *= a;
    this.y *= a;
    this.z *= a;
    return this;
  }

  public Vec3d mul(double x, double y, double z) {
    this.x *= x;
    this.y *= y;
    this.z *= z;
    return this;
  }

  public double distanceTo(Vec3d vec) {
    double sx = Math.abs(vec.x - x);
    sx *= sx;
    double sy = Math.abs(vec.y - y);
    sy *= sy;
    double sz = Math.abs(vec.z - z);
    sz *= sz;
    return Math.sqrt(sx + sy + sz);
  }

  public Vec3d copy() {
    return new Vec3d(x, y, z);
  }

  @Override
  public String toString() {
    return String.format("Vec2d(%f, %f)", x, y);
  }
}
