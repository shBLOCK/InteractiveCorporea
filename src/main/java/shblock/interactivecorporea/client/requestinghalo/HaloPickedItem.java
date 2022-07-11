package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.helper.Vector3;

public class HaloPickedItem {
  private final ItemStack stack;

  private Vector3 pos;
  private Vector3 targetPos;
  private Vector3 spd = new Vector3(0, 0, 0);

  // x: pitch   y: yaw
  private final Vec2d rot;
  private final Vec2d targetRot;
  private final Vec2d rotSpd = new Vec2d();

  private double fade = 0;
  private boolean fadingOut = false;

  public HaloPickedItem(ItemStack stack, Vector3 pos, double pitch, double yaw) {
    this.stack = stack;

    this.pos = pos;
    this.targetPos = pos;
    this.rot = new Vec2d(pitch, yaw);
    this.targetRot = rot.copy();
  }

  public void setTargetPosition(Vector3 target) {
    this.targetPos = target;
  }

  public void setTargetRotation(double pitch, double yaw) {
    targetRot.set(pitch, yaw);
  }

  public void setTargetRotationDegrees(double pitch, double yaw) {
    setTargetRotation(Math.toRadians(pitch), Math.toRadians(yaw));
  }

  public boolean render(MatrixStack ms) {
    if (fadingOut) {
      fade -= RenderTick.delta / 5;
      if (fade < 0) return true;
    } else {
      fade += RenderTick.delta / 5;
      if (fade > 1)
        fade = 1;
    }

    pos = pos.add(spd.multiply(RenderTick.delta));
    rot.add(rotSpd.copy().mul(RenderTick.delta));

    ms.push();

    ms.translate(pos.x, pos.y, pos.z);

    ms.rotate(Vector3f.YP.rotation((float) rot.y));
    ms.rotate(Vector3f.XP.rotation((float) rot.x));

    float scale = (float) (Math.sin(fade * Math.PI / 2) * .3);
    ms.scale(scale, scale, 1F);

    RenderUtil.applyStippling(Math.sin(RenderTick.total / 5) / 4 + 10, () -> RenderUtil.renderFlatItem(ms, stack));

    ms.pop();

    return false;
  }

  public void fadeOut() {
    fadingOut = true;
  }

  public void tick() {
    spd = MathUtil.smoothMovingSpeed(pos, targetPos, spd, .1, 1, .01);
    rotSpd.set(MathUtil.smoothMovingSpeed(rot, targetRot, rotSpd, .1, .8, .01));
  }

  public ItemStack getStack() {
    return stack;
  }
}
