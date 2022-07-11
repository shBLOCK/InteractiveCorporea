package shblock.interactivecorporea.client.requestinghalo.crafting;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;

import javax.annotation.Nullable;

public class CraftingInterfaceSlot {
  private static final Minecraft mc = Minecraft.getInstance();

  private final HaloCraftingInterface craftingInterface;
  private final int slot;
  private final Vec2d localPos;

  private static final double SPACING = .6;
  private static final double SIZE = .25;

  private double mouseOverAnimation = 0;

  private ItemStack shadowStack = ItemStack.EMPTY; // crafting shouldn't be performed here, this is always a shadow
  private ItemStack oldShadowStack = ItemStack.EMPTY;
  private double changeShadowStackAnimation = 0;

  public CraftingInterfaceSlot(HaloCraftingInterface craftingInterface, int slot) {
    this.craftingInterface = craftingInterface;
    this.slot = slot;
    int x = -(slot % 3 - 1);
    int y = -(slot / 3 - 1);
    this.localPos = new Vec2d(x * SPACING, y * SPACING);
  }

  public CraftingInterfaceSlot(HaloCraftingInterface craftingInterface, int slot, ItemStack initialShadowStack) {
    this(craftingInterface, slot);
    this.shadowStack = initialShadowStack;
  }

  public void render(MatrixStack ms, Vec2d pointingLocalPos) {
    boolean shouldRenderOldStack = true;
    changeShadowStackAnimation += RenderTick.delta / 5;
    if (changeShadowStackAnimation > 1) {
      changeShadowStackAnimation = 1;
      shouldRenderOldStack = false;
    }

    if (isPointIn(pointingLocalPos)) {
      mouseOverAnimation = 1;
    } else {
      mouseOverAnimation -= RenderTick.delta / 3;
      if (mouseOverAnimation < 0)
        mouseOverAnimation = 0;
    }

    ms.push();
    ms.translate(localPos.x, 0, localPos.y);
    double mouseOverFactor = (1 - Math.cos(mouseOverAnimation * Math.PI)) / 2;
    float size = (float) (mouseOverFactor * .05 + SIZE);
    ms.scale(size, size, size);

    //TODO: bg color based on item availability
    float r = .97F;
    float g = 0F;
    float b = .98F;
    float a = (float) (mouseOverFactor * .3 + .6);
    renderBg(ms, r, g, b, a);

    ms.translate(0, .01, 0);

    float sineFactor = (float) ((1 - Math.cos(changeShadowStackAnimation * Math.PI)) / 2);
    float newSize = 0, oldSize = 0;
    if (!shadowStack.isEmpty() && !oldShadowStack.isEmpty()) {
      newSize = Math.max(0F, sineFactor - .5F) * 2F;
      oldSize = Math.max(0F, 1F - sineFactor - .5F) * 2F;
    } else if (!shadowStack.isEmpty()) {
      newSize = sineFactor;
      oldSize = 0F;
    } else if (!oldShadowStack.isEmpty()) {
      newSize = 0F;
      oldSize = 1F - sineFactor;
    }
    newSize *= 2;
    oldSize *= 2;

    if (!shadowStack.isEmpty()) {
      ms.push();
      ms.scale(newSize, 1, newSize);
      ms.rotate(Vector3f.XP.rotationDegrees(90));
      ms.rotate(Vector3f.YP.rotationDegrees(180));
      RenderUtil.applyStippling(11, () -> RenderUtil.renderFlatItem(ms, shadowStack));
      ms.pop();
    }
    if (!oldShadowStack.isEmpty() && shouldRenderOldStack) {
      ms.push();
      ms.scale(oldSize, 1, oldSize);
      ms.rotate(Vector3f.XP.rotationDegrees(90));
      ms.rotate(Vector3f.YP.rotationDegrees(180));
      RenderUtil.applyStippling(11, () -> RenderUtil.renderFlatItem(ms, oldShadowStack));
      ms.pop();
    }

    renderRealItem(ms);

    ms.pop();
  }

  private void renderBg(MatrixStack ms, float r, float g, float b, float a) {
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    IVertexBuilder builder = buffers.getBuffer(ModRenderTypes.craftingSlotBg);
    Matrix4f matrix = ms.getLast().getMatrix();

    builder.pos(matrix, +1, 0, +1).color(r, g, b, a).endVertex();
    builder.pos(matrix, +1, 0, -1).color(r, g, b, a).endVertex();
    builder.pos(matrix, -1, 0, -1).color(r, g, b, a).endVertex();
    builder.pos(matrix, -1, 0, +1).color(r, g, b, a).endVertex();

    buffers.finish(ModRenderTypes.craftingSlotBg);
  }

  private void renderRealItem(MatrixStack ms) { //TODO!!!!!!!!!!: cache the real item (server send update packet to update the cache)
    ItemStack stack = ItemRequestingHalo.getStackInCraftingSlot(craftingInterface.haloStack, slot);
    if (stack.isEmpty()) return;
    ms.push();
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    ms.scale(5, 5, 5);
    ms.translate(0, .1, 0);
    mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, 0xF000F0, OverlayTexture.NO_OVERLAY, ms, buffers);
    buffers.finish();
    ms.pop();
  }

  public boolean setShadowStack(ItemStack newStack) {
    if (changeShadowStackAnimation < .99) return false;

    if (newStack.isEmpty() && shadowStack.isEmpty()) return false;
    if (!newStack.isEmpty() && !shadowStack.isEmpty()) {
      if (ItemStack.areItemStacksEqual(newStack, shadowStack)) {
        return false;
      }
    }

    oldShadowStack = shadowStack;
    shadowStack = newStack.copy();
    changeShadowStackAnimation = 0;
    return true;
  }

  public boolean isPointIn(Vec2d localPoint) {
    Vec2d diff = localPoint.copy().sub(localPos);
    return Math.abs(diff.x) < SIZE && Math.abs(diff.y) < SIZE;
  }

  public ItemStack getShadowStack() {
    return shadowStack;
  }

  public int getSlotIndex() {
    return slot;
  }

  public ItemStack getRealStack() {
    return ItemRequestingHalo.getStackInCraftingSlot(craftingInterface.haloStack, slot);
  }
}
