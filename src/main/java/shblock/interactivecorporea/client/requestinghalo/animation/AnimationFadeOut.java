package shblock.interactivecorporea.client.requestinghalo.animation;

public class AnimationFadeOut extends AbstractAnimation {
  public AnimationFadeOut(double length) {
    super(length);
  }

  public float getScale() {
    return (float) Math.sin(Math.PI * time * .5F);
  }

  public int getAlpha() {
    double p = Math.sin(Math.PI * time * .5F);
    return (int) (p * 255);
  }
}
