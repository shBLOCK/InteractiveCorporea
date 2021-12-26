package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ColorHelper;
import shblock.interactivecorporea.client.requestinghalo.animation.*;
import shblock.interactivecorporea.common.util.Perlin;
import shblock.interactivecorporea.common.util.Vec2f;
import shblock.interactivecorporea.common.util.Vec2i;
import vazkii.botania.client.core.helper.RenderHelper;

import java.util.Random;

public class AnimatedItemStack {
  private static final Minecraft mc = Minecraft.getInstance();
  public Perlin noise = new Perlin();

  private ItemStack stack;
  //  private boolean shouldDisplay = true;
  private boolean removed = false;
  private Vec2i posi = new Vec2i();
  private Vec2f pos = new Vec2f();
  private AnimationFadeIn animationFadeIn;
  private AnimationFadeOut animationFadeOut;
  private AnimationMove animationMove;
  private AnimationChangeAmount animationChangeAmount;
  private boolean isNew = true;

  public AnimatedItemStack(ItemStack stack) {
    this.stack = stack;
  }

  /**
   * Update the animation (Called every frame, no matter if the item is in the render area of not)
   * @return If the stack should be removed
   */
  public boolean update(float dt) {
    if (animationFadeIn != null) {
      if (animationFadeIn.update(dt)) {
        animationFadeIn = null;
      }
    }
    if (animationFadeOut != null) {
      if (animationFadeOut.update(dt)) {
        animationFadeOut = null;
        return true;
      }
    }
    if (animationMove != null) {
      if (animationMove.update(dt)) {
        pos.set(posi.x, posi.y);
        animationMove = null;
      } else {
        animationMove.getCurrentPos(pos, posi); // Update the float position
      }
    }
    if (animationChangeAmount != null) {
      if (animationChangeAmount.update(dt)) {
        animationChangeAmount = null;
      }
    }

    isNew = false;

    return false;
  }

  public void renderItem(MatrixStack ms) {
    ms.push();
    if (animationFadeIn != null) {
      float scale = animationFadeIn.getScale();
      ms.scale(scale, scale, scale);
    }

    //TODO: use changing alpha instead
    if (animationFadeOut != null) {
      float scale = animationFadeOut.getScale();
      ms.scale(scale, scale, scale);
    }
    mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GUI, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, mc.getRenderTypeBuffers().getBufferSource());

//    if (animationFadeOut != null) {
//      ms.translate(0.01, 0.01, 0.01);
//    }
//    int alpha = animationFadeOut == null ? 255 : animationFadeOut.getAlpha();
//    RenderHelper.renderItemCustomColor(mc.player, stack, ColorHelper.PackedColor.packColor(alpha, 255, 255, 255), ms, mc.getRenderTypeBuffers().getBufferSource(), 0xF000F0, OverlayTexture.NO_OVERLAY);

    ms.pop();
  }

  public void renderAmount(MatrixStack ms) {
    ms.push();
    //TODO
    ms.pop();
  }

  public ItemStack getStack() {
    return stack;
  }

  public void playFadeIn(float length) {
//    shouldDisplay = true;
    animationFadeIn = new AnimationFadeIn(length);
  }

  public boolean isNew() {
    return isNew;
  }

//  public void playFadeOut(float length) {
//    shouldDisplay = false;
//    animationFadeOut = new AnimationFadeOut(length);
//  }

  public void remove(float animationLength) {
    removed = true;
    animationFadeOut = new AnimationFadeOut(animationLength);
  }

  public boolean isRemoved() {
    return removed;
  }

//  public boolean shouldDisplay() {
//    return shouldDisplay;
//  }

  public void setPos(int x, int y) {
    posi.set(x, y);
    pos.set(x, y);
  }

  public void moveTo(Vec2i dest, float animationLength) {
    animationMove = new AnimationMove(animationLength, posi);
    posi = dest;
  }

  public Vec2i getPrevPosi() {
    return animationMove == null ? posi : animationMove.getPrev();
  }

  public Vec2i getPosi() {
    return posi;
  }

  public Vec2f getPos() {
    return pos;
  }

  public void changeAmount(int amount, float animationLength) {
    this.animationChangeAmount = new AnimationChangeAmount(animationLength, stack.getCount());
    stack.setCount(amount);
  }
}
