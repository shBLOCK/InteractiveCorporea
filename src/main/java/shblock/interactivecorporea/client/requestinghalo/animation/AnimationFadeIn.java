package shblock.interactivecorporea.client.requestinghalo.animation;

public class AnimationFadeIn extends AbstractAnimation {
  public AnimationFadeIn(float length) {
    super(length);
  }

  public float getScale() {
    return (float) Math.cos(Math.PI * time * .5F);
  }
}
