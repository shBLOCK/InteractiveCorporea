package shblock.interactivecorporea.client.requestinghalo.animation;

public class AnimationChangeAmount extends AbstractAnimation {
  private final int prevAmount;

  public AnimationChangeAmount(double length, int prevAmount) {
    super(length);
    this.prevAmount = prevAmount;
  }

  public int getPrevAmount() {
    return prevAmount;
  }

  public double getProgress() {
    return Math.sin(Math.PI * .5F * time);
  }
}
