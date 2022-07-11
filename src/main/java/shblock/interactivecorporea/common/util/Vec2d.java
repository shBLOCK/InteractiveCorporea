package shblock.interactivecorporea.common.util;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import vazkii.botania.common.core.helper.Vector3;

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

  public Vec2d(Vector3 vec) {
    this.x = vec.x;
    this.y = vec.z;
  }

  public Vec2d set(Vec2d vec) {
    x = vec.x;
    y = vec.y;
    return this;
  }

  public Vec2d set(double x, double y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Vec2d add(Vec2d vec) {
    this.x += vec.x;
    this.y += vec.y;
    return this;
  }

  public Vec2d add(double x, double y) {
    this.x += x;
    this.y += y;
    return this;
  }

  public Vec2d sub(Vec2d vec) {
    this.x -= vec.x;
    this.y -= vec.y;
    return this;
  }

  public Vec2d sub(double x, double y) {
    this.x -= x;
    this.y -= y;
    return this;
  }

  public Vec2d mul(double a) {
    this.x *= a;
    this.y *= a;
    return this;
  }

  public Vec2d mul(double x, double y) {
    this.x *= x;
    this.y *= y;
    return this;
  }

  public double distanceTo(Vec2d vec) {
    double sx = Math.abs(vec.x - x);
    sx *= sx;
    double sy = Math.abs(vec.y - y);
    sy *= sy;
    return Math.sqrt(sx + sy);
  }

  public double length() {
    return Math.sqrt(x * x + y * y);
  }

  public Vec2d apply(Double2DoubleFunction func) {
    this.x = func.applyAsDouble(x);
    this.y = func.applyAsDouble(y);
    return this;
  }

  public Vec2d copy() {
    return new Vec2d(x, y);
  }

  public Vector3 toVector3() {
    return new Vector3(x, 0, y);
  }

  @Override
  public String toString() {
    return String.format("Vec2d(%f, %f)", x, y);
  }
}
