package shblock.interactivecorporea.common.util;

public class Vec2f {
  public float x;
  public float y;

  public Vec2f() {
    this.x = 0F;
    this.y = 0F;
  }

  public Vec2f(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public void set(Vec2f vec) {
    x = vec.x;
    y = vec.y;
  }

  public void set(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
