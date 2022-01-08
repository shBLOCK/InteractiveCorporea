package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import shblock.interactivecorporea.client.requestinghalo.animation.*;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.common.util.*;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.List;

//TODO: remake the animation system, use a system similar to the selection box, but use a changing speed system so that is looks smooth
@MethodsReturnNonnullByDefault
public class AnimatedItemStack {
  private static final Minecraft mc = Minecraft.getInstance();
  public Perlin noise = new Perlin();

  private final ItemStack stack;
  private boolean removed = false;
  private Vec2i posi = new Vec2i();
  private final Vec2d pos = new Vec2d();
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
  public boolean update(double dt) {
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
        animationMove.getCurrentPos(pos, posi); // Update the double position
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

  private void setupForFadeAnimation(MatrixStack ms) {
    if (animationFadeIn != null) {
      float scale = animationFadeIn.getScale();
      ms.scale(scale, scale, scale);
    }

    //TODO: use changing alpha instead
    if (animationFadeOut != null) {
      float scale = animationFadeOut.getScale();
      ms.scale(scale, scale, scale);
    }
  }

  public void renderItem(MatrixStack ms, IRenderTypeBuffer.Impl buffers) {
    ms.push();

    setupForFadeAnimation(ms);

    RenderHelper.setupDiffuseGuiLighting(ms.getLast().getMatrix());
    IBakedModel ibakedmodel = mc.getItemRenderer().getItemModelWithOverrides(stack, mc.world, mc.player);
    mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, ibakedmodel);

    ms.pop();
  }

  public void renderAmount(MatrixStack ms, int color, IRenderTypeBuffer.Impl buffers) {
    ms.push();

    setupForFadeAnimation(ms);

    String text = TextHelper.formatBigNumber(stack.getCount(), true);

    if (animationChangeAmount != null) {
      double spacing = 9F;
      double p = animationChangeAmount.getProgress();
      double pp = p;
      boolean rev = animationChangeAmount.getPrevAmount() < stack.getCount();
      if (rev) {
        p = -p;
      } else {
        ms.translate(0F, -spacing, 0F);
      }
      String prevText = TextHelper.formatBigNumber(animationChangeAmount.getPrevAmount(), true);
      int r = ColorHelper.PackedColor.getRed(color);
      int g = ColorHelper.PackedColor.getGreen(color);
      int b = ColorHelper.PackedColor.getBlue(color);
      int alpha = ColorHelper.PackedColor.getAlpha(color);
      if (alpha > 251) {
        alpha = 251;
      }
      ms.translate(0F, p * spacing, 0F);
      renderAmountText(ms, rev ? text : prevText, ColorHelper.PackedColor.packColor((int) (alpha * (rev ? (1F - pp) : pp)) + 4, r, g, b), buffers);
      ms.translate(0F, spacing, 0F);
      renderAmountText(ms, rev ? prevText : text, ColorHelper.PackedColor.packColor((int) (alpha * (rev ? pp : (1F - pp))) + 4, r, g, b), buffers);
    } else {
      renderAmountText(ms, text, color, buffers);
    }

    ms.pop();
  }

  private void renderAmountText(MatrixStack ms, String text, int color, IRenderTypeBuffer.Impl buffers) {
    ms.push();

    FontRenderer font = mc.fontRenderer;

    ms.rotate(new Quaternion(Vector3f.XP, 180, true));
    ms.rotate(new Quaternion(Vector3f.YP, 180, true));
    double w = font.getCharacterManager().func_238350_a_(text);
    ms.translate(-w, 0, 0);

    Matrix4f mat = ms.getLast().getMatrix();

    font.renderString(text, 0, 0, color, false, mat, buffers, false, 0, 0xF000F0);

    ms.translate(0, 0, 0.01);
    mat = ms.getLast().getMatrix();

    int shadeColor = (color & 16579836) >> 2 | color & -16777216;
    font.renderString(text, 1, 1, shadeColor, false, mat, buffers, false, 0, 0xF000F0);

    ms.pop();
  }

  public void renderHud(MatrixStack ms, float pt, MainWindow window) {
    if (mc.player == null) return;

    if (KeyboardHelper.hasAltDown()) {
      ms.push();
      List<ITextComponent> tooltip = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
      GuiUtils.preItemToolTip(stack);
      FontRenderer font = stack.getItem().getFontRenderer(stack);
      if (font == null) {
        font = mc.fontRenderer;
      }
      int width = window.getScaledWidth();
      int height = window.getScaledHeight();
      int bgCol = MathHelper.hsvToRGB(ClientTickHandler.total % 200F / 200F, 1F, 1F);
      bgCol |= ((int) (((Math.sin(ClientTickHandler.total / 10F) + 1F) * .03F + .04F) * 0xFF)) << 24;
      int borderColStart = MathHelper.hsvToRGB((ClientTickHandler.total + 66.66F) % 200F / 200F, 1F, 1F) | 0xFF000000;
      int borderColEnd = MathHelper.hsvToRGB((ClientTickHandler.total + 133.33F) % 200F / 200F, 1F, 1F) | 0xFF000000;
      GuiUtils.drawHoveringText(
          stack,
          ms,
          tooltip,
          width / 2,
          height / 2,
          width,
          height,
          100,
          bgCol,
          borderColStart,
          borderColEnd,
          font
      );
      ms.pop();

      final MatrixStack itemMS = new MatrixStack();
      itemMS.scale(3F, 3F, 3F);
      ItemRenderHelper.renderItemAndEffectIntoGUI(stack, width / 2 - 48, height / 2 - 8, itemMS);
    }
  }

  public ItemStack getStack() {
    return stack;
  }

  public void playFadeIn(double length) {
//    shouldDisplay = true;
    animationFadeIn = new AnimationFadeIn(length);
  }

  public boolean isNew() {
    return isNew;
  }

//  public void playFadeOut(double length) {
//    shouldDisplay = false;
//    animationFadeOut = new AnimationFadeOut(length);
//  }

  public void remove() {
    removed = true;
  }

  public void removeWithAnimation(double animationLength) {
    remove();
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

  public void moveTo(Vec2i dest, double animationLength) {
    animationMove = new AnimationMove(animationLength, posi);
    posi = dest;
  }

  public Vec2i getPrevPosi() {
    return animationMove == null ? posi : animationMove.getPrev();
  }

  public Vec2i getPosi() {
    return posi;
  }

  public Vec2d getPos() {
    return pos;
  }

  public void changeAmount(int amount, double animationLength) {
    this.animationChangeAmount = new AnimationChangeAmount(animationLength, stack.getCount());
    stack.setCount(amount);
  }
}
