package shblock.interactivecorporea.common.util;

public class Vec2i {
  public int x;
  public int y;

  public Vec2i() {
    this.x = 0;
    this.y = 0;
  }

  public Vec2i(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void set(Vec2i vec) {
    x = vec.x;
    y = vec.y;
  }

  public void set(int x, int y) {
    this.x = x;
    this.y = y;
  }
}
