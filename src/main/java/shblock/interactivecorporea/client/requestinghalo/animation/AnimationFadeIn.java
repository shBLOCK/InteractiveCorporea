package shblock.interactivecorporea.client.requestinghalo.animation;

public class AnimationFadeIn extends AbstractAnimation {
  public AnimationFadeIn(double length) {
    super(length);
  }

  public float getScale() {
    return (float) Math.cos(Math.PI * time * .5F);
  }
}
