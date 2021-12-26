package shblock.interactivecorporea.client.requestinghalo.animation;

public class AnimationChangeAmount extends AbstractAnimation {
  private final int prevAmount;

  public AnimationChangeAmount(float length, int prevAmount) {
    super(length);
    this.prevAmount = prevAmount;
  }

  public int getPrevAmount() {
    return prevAmount;
  }

  public float getProgress() {
    return time;
  }
}
