package shblock.interactivecorporea.client.requestinghalo.animation;

public class AbstractAnimation {
  protected double time = 1F;
  private final double speed;

  /**
   * @param length The ticks it takes for this animation to complete
   */
  public AbstractAnimation(double length) {
    speed = 1F / length;
  }

  /**
   * Update the animation (Called every frame)
   * @return If the animation should be removed (If the animation is done)
   */
  public boolean update(double dt) {
    time -= speed * dt;
    if (time < 0F) {
      time = 0F;
      return true;
    }
    return false;
  }
}
