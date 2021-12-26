package shblock.interactivecorporea.client.requestinghalo.animation;

import shblock.interactivecorporea.common.util.Vec2f;
import shblock.interactivecorporea.common.util.Vec2i;

public class AnimationMove extends AbstractAnimation {
  private final Vec2i prev;

  public AnimationMove(float length, Vec2i prev) {
    super(length);
    this.prev = prev;
  }

  public Vec2i getPrev() {
    return prev;
  }

  /**
   * @param pos The Vec2f object to store the result
   */
  public Vec2f getCurrentPos(Vec2f pos, Vec2i dest) {
    float p = (float) Math.cos(Math.PI * time * .5F);
    pos.x = prev.x + (dest.x - prev.x) * p;
    pos.y = prev.y + (dest.y - prev.y) * p;
    return pos;
  }

  public Vec2f getCurrentPos(Vec2i dest) {
    return getCurrentPos(new Vec2f(), dest);
  }
}
