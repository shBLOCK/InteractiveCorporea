package shblock.interactivecorporea.client.requestinghalo.animation;

public class AnimationFadeOut extends AbstractAnimation {
  public AnimationFadeOut(float length) {
    super(length);
  }

  public float getScale() {
    return (float) Math.sin(Math.PI * time * .5F);
  }

  public int getAlpha() {
    float p = (float) Math.sin(Math.PI * time * .5F);
    return (int) (p * 255);
  }
}
