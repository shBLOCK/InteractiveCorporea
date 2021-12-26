package shblock.interactivecorporea.client.requestinghalo.animation;

public class AbstractAnimation {
  protected float time = 1F;
  private final float speed;

  /**
   * @param length The ticks it takes for this animation to complete
   */
  public AbstractAnimation(float length) {
    speed = 1F / length;
  }

  /**
   * Update the animation (Called every frame)
   * @return If the animation should be removed (If the animation is done)
   */
  public boolean update(float dt) {
    time -= speed * dt;
    if (time < 0F) {
      time = 0F;
      return true;
    }
    return false;
  }
}
