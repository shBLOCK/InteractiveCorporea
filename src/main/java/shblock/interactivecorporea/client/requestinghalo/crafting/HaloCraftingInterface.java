package shblock.interactivecorporea.client.requestinghalo.crafting;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.common.util.Constants;
import shblock.interactivecorporea.client.jei.DummyTransferringGui;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.render.shader.SimpleShaderProgram;
import shblock.interactivecorporea.client.requestinghalo.HaloPickedItem;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.network.CPacketChangeStackInHaloCraftingSlot;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.util.StackHelper;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.lwjgl.opengl.GL43.*;

//TODO
// slot mouse over sound effect
// slot bg color (red if no item in network, blue if no item in network but is in player inventory, green if have item in network)
// requesting items for craft
// render 3d floating item model when item arrived
// crafting animation (when crafting, reverse the edge flowing)
// if close interface when crafting, drop items

public class HaloCraftingInterface {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final SimpleShaderProgram bgShader = new SimpleShaderProgram("common_120_world", "halo_crafting_bg", Uniforms::init);
  private static class Uniforms {
    private static int TIME;
    private static int MOUSE_OVER_ANIMATION;
    private static int BG_SIZE;
    private static int EDGE_FLOWING_TIME;

    private static void init(SimpleShaderProgram shader) {
      TIME = shader.getUniformLocation("time");
      MOUSE_OVER_ANIMATION = shader.getUniformLocation("mouseOverAnimation");
      BG_SIZE = shader.getUniformLocation("bgSize");
      EDGE_FLOWING_TIME = shader.getUniformLocation("edgeFlowingTime");
    }
  }

  public final CISlotPointer haloItemSlot;
  public ItemStack haloStack;

  private double rotation = 0;
  private double targetRotation = 0;
  private double rotationSpd = 0;
  private double pos = 0;
  private double size = 1;

  private Vec2d pointingLocalPos = new Vec2d();

  private double mouseOverAnimation = 0;
  private double edgeFlowingTime = 0;

  private final CraftingInterfaceSlot[] slots = new CraftingInterfaceSlot[9];
  private ICraftingRecipe currentRecipe = null;
  private ItemStack currentOutput = ItemStack.EMPTY;
  private NonNullList<ItemStack> currentRemainingItems = NonNullList.withSize(9, ItemStack.EMPTY);
  private double craftingOutputAnimation = 0;
  private final Map<ItemStack, Double> fadingCraftingOutputs = new HashMap<>();

  public HaloCraftingInterface(CISlotPointer haloItemSlot, ItemStack haloStack) {
    this.haloItemSlot = haloItemSlot;
    this.haloStack = haloStack;

    ListNBT shadowNBTList = getOrCreateListNBTForShadow();
    for (int i = 0; i < 9; i++) {
      slots[i] = new CraftingInterfaceSlot(this, i, ItemStack.read(shadowNBTList.getCompound(i)));
    }
  }

  public void render(MatrixStack ms, double openCloseAnimation) {
    rotation += rotationSpd * RenderTick.delta;

    if (isPointingAtInterface()) {
      mouseOverAnimation += RenderTick.delta / 10;
    } else {
      mouseOverAnimation -= RenderTick.delta / 10;
    }
    mouseOverAnimation = MathHelper.clamp(mouseOverAnimation, 0, 1);

    if (!currentOutput.isEmpty()) {
      craftingOutputAnimation += RenderTick.delta / 8;
      if (craftingOutputAnimation > 1)
        craftingOutputAnimation = 1;
    } else {
      craftingOutputAnimation = 0;
    }

    ms.push();

    ms.rotate(Vector3f.YP.rotation((float) -rotation));
    ms.translate(0, 0, pos);
    float scale = (float) (size * openCloseAnimation);
    ms.scale(scale, scale, scale);

    bgShader.use();
    glUniform1f(Uniforms.TIME, (float) (RenderTick.total / 20));
    double mouseOverFactor = (1 - Math.cos(mouseOverAnimation * Math.PI)) / 2;
    glUniform1f(Uniforms.MOUSE_OVER_ANIMATION, (float) mouseOverAnimation);
    edgeFlowingTime += RenderTick.delta * (mouseOverAnimation * 1.5 + .5);
    glUniform1f(Uniforms.EDGE_FLOWING_TIME, (float) edgeFlowingTime);
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    IVertexBuilder builder = buffers.getBuffer(ModRenderTypes.craftingBg);
    Matrix4f matrix = ms.getLast().getMatrix();
    float r=0F, g=.5F, b=1F, a=.6F;
    float s = (float) (1 + mouseOverFactor * .3);
    glUniform1f(Uniforms.BG_SIZE, s);
    builder.pos(matrix, +s, 0, +s).color(r, g, b, a).tex(+s, +s).endVertex();
    builder.pos(matrix, +s, 0, -s).color(r, g, b, a).tex(+s, -s).endVertex();
    builder.pos(matrix, -s, 0, -s).color(r, g, b, a).tex(-s, -s).endVertex();
    builder.pos(matrix, -s, 0, +s).color(r, g, b, a).tex(-s, +s).endVertex();
    buffers.finish(ModRenderTypes.craftingBg);
    bgShader.release();

    ms.push();
    ms.translate(0, .01, 0);
    for (CraftingInterfaceSlot slot : slots) {
      slot.render(ms, pointingLocalPos);
    }
    ms.pop();

    if (!currentOutput.isEmpty()) {
      renderOutputItem(ms, currentOutput, craftingOutputAnimation, 1);
    }

    fadingCraftingOutputs.replaceAll((tmpS, t) -> t - RenderTick.delta / 8);
    List<ItemStack> toRemoveList = new ArrayList<>();
    for (Map.Entry<ItemStack, Double> entry : fadingCraftingOutputs.entrySet()) {
      double progress = entry.getValue();
      if (progress < 0) {
        toRemoveList.add(entry.getKey());
      } else {
        renderOutputItem(ms, entry.getKey(), 1, progress);
      }
    }
    toRemoveList.forEach(fadingCraftingOutputs::remove);

    ms.pop();
  }

  private void renderOutputItem(MatrixStack ms, ItemStack stack, double normalizedScale, double alpha) {
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    RenderUtil.applyStippling(16, () -> {
      RenderSystem.pushLightingAttributes();
      ms.push();
      float scale = (float) (3 * (1 - Math.cos(normalizedScale * Math.PI)) / 2);
      ms.scale(scale, scale, scale);
      ms.translate(0, .2, 0);
      ms.rotate(Vector3f.YP.rotation((float) (RenderTick.total / 15)));
      ItemRenderer itemRenderer = mc.getItemRenderer();
      IBakedModel model = itemRenderer.getItemModelWithOverrides(stack, mc.world, null);
//        if (model.isGui3d()) {
//          RenderHelper.setupGui3DDiffuseLighting();
//        } else {
      RenderHelper.setupGuiFlatDiffuseLighting();
//        }
      RenderUtil.applyStippling(alpha * (Math.sin(RenderTick.total / 5) * .2 + .75), () -> {
        itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GROUND, false, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
        mc.getRenderTypeBuffers().getBufferSource().finish();
      });

      ms.pop();
      RenderSystem.popAttributes();
    });
  }

  public void tick(@Nullable Vec2d worldPos2d) {
    rotationSpd = MathUtil.smoothMovingSpeed(rotation, targetRotation, rotationSpd, .1, .8, .01);

    if (worldPos2d != null) {
      pointingLocalPos = toLocalPos(worldPos2d);
    }
  }

  public boolean tryOpenJei() {
    if (isPointingAtInterface()) {
      mc.displayGuiScreen(new DummyTransferringGui());
      return true;
    }
    return false;
  }

  private ListNBT getOrCreateListNBTForShadow() {
    ListNBT list = ItemNBTHelper.getList(haloStack, "crafting_slot_shadow_items", Constants.NBT.TAG_COMPOUND, false);
    for (int i = list.size(); i < 9; i++) {
      list.add(new CompoundNBT());
    }
    return list;
  }

  private void saveShadowItemToNBT(int slot, ItemStack shadow) {
    ListNBT list = getOrCreateListNBTForShadow();
    list.set(slot, shadow.write(new CompoundNBT()));
  }

  /**
   * Remember to call updateRecipe() after changing all the slots.
   * This is to prevent updating the recipe when changing every item
   */
  public boolean tryPlaceShadowItem(int slot, ItemStack stack) {
    if (slots[slot].setShadowStack(stack)) {
      saveShadowItemToNBT(slot, stack);
      return true;
    }
    return false;
  }

//  public boolean tryPlaceShadowItem(ItemStack stack) {
//    CraftingInterfaceSlot slot = getPointingSlot();
//    if (slot == null) return false;
//    boolean result = slot.setShadowStack(stack);
//    if (result)
//      updateRecipe();
//    return result;
//  }

  public boolean handleSlotInteraction(boolean isPut, @Nullable Vector3 clickWorldPos, @Nullable HaloPickedItem pickedItem) {
    if (mc.player == null || mc.world == null) return false;
    if (clickWorldPos == null) return false;
    CraftingInterfaceSlot slot = getPointingSlot();
    if (slot == null) return false;

    ItemStack newStack = mc.player.getHeldItemMainhand();
    ItemStack shadowStack = slot.getShadowStack();
    ItemStack realStack = slot.getRealStack();

    if (isPut) { // put the item in player's hand into the slot (or replace the item in that slot)
      if (newStack.isEmpty()) {
        if (pickedItem == null) return false;
        ItemStack pickedStack = pickedItem.getStack();
        if (tryPlaceShadowItem(slot.getSlotIndex(), pickedStack)) {
          updateRecipe();
          return true;
        }
      }

      if (realStack.isEmpty()) { // if the realStack was empty (put)
        if (StackHelper.equalItemAndTag(newStack, shadowStack) || tryPlaceShadowItem(slot.getSlotIndex(), newStack)) {
          updateRecipe();
          ModPacketHandler.sendToServer(new CPacketChangeStackInHaloCraftingSlot(haloItemSlot, slot.getSlotIndex(), true, clickWorldPos));
          return true;
        }
      }
      // if the realStack's item is the same as the newStack's and the realStack has not reached the stack limit (add)
      if (realStack.getCount() < realStack.getMaxStackSize() && StackHelper.equalItemAndTag(newStack, realStack)) {
        ModPacketHandler.sendToServer(new CPacketChangeStackInHaloCraftingSlot(haloItemSlot, slot.getSlotIndex(), true, clickWorldPos));
        return true;
      } else { // the realStack does not match the newStack (replace)
        if (tryPlaceShadowItem(slot.getSlotIndex(), newStack)) {
          updateRecipe();
          ModPacketHandler.sendToServer(new CPacketChangeStackInHaloCraftingSlot(haloItemSlot, slot.getSlotIndex(), true, clickWorldPos));
          return true;
        }
      }
    } else { // remove the item in the slot
      if (!realStack.isEmpty()) {
        ModPacketHandler.sendToServer(new CPacketChangeStackInHaloCraftingSlot(haloItemSlot, slot.getSlotIndex(), false, clickWorldPos));
        return true;
      }
      if (!shadowStack.isEmpty()) {
        if (tryPlaceShadowItem(slot.getSlotIndex(), ItemStack.EMPTY)) {
          updateRecipe();
          return true;
        }
      }
    }

    return false;
  }

//  /**
//   * @return this only represents if the packet was successfully sent to the server, the action on server might still fail
//   */
//  public boolean sendChangeItemInSlotPacket(CISlotPointer halo, ItemStack haloStack, boolean isPut, Vector3 clickPosition) {
//
//    if (isPut && mc.player.getHeldItemMainhand().isEmpty()) return false;
//    CraftingInterfaceSlot slot = getPointingSlot();
//    if (slot == null) return false;
//    if (haloStack.isEmpty()) return false;
//    if (!isPut && ItemRequestingHalo.getStackInCraftingSlot(haloStack, slot.getSlotIndex()).isEmpty()) return false;
//    ModPacketHandler.sendToServer(new PacketChangeStackInHaloCraftingSlot(halo, slot.getSlotIndex(), isPut, clickPosition));
//    return true;
//  }

  public void updateRecipe() {
    if (mc.world == null || mc.player == null) return;

    RecipeManager manager = mc.world.getRecipeManager();
    CraftingInventory craftingInv = new CraftingInventory(new Container(ContainerType.CRAFTING, -1) {
      @Override
      public boolean canInteractWith(@Nonnull PlayerEntity player) {
        return false;
      }
    }, 3, 3);
    for (int i = 0; i < 9; i++) {
      ItemStack stack = slots[i].getShadowStack();
      if (stack.isEmpty()) continue;
      stack = stack.copy();
      stack.setCount(1);
      craftingInv.setInventorySlotContents(i, stack);
    }

    Optional<ICraftingRecipe> recipe = manager.getRecipe(IRecipeType.CRAFTING, craftingInv, mc.world);
    currentRecipe = recipe.orElse(null);

    ItemStack oldOutput = currentOutput.copy();

    if (currentRecipe != null) {
      currentOutput = currentRecipe.getCraftingResult(craftingInv);
      currentRemainingItems = currentRecipe.getRemainingItems(craftingInv);
    } else {
      currentOutput = ItemStack.EMPTY;
      currentRemainingItems = NonNullList.withSize(9, ItemStack.EMPTY);
    }

    if (!oldOutput.isEmpty() && !ItemStack.areItemStacksEqual(oldOutput, currentOutput)) {
      fadingCraftingOutputs.put(oldOutput, 1D);
      craftingOutputAnimation = 0;
    }
  }

  /**
   * Do the crafting of the current recipe.
   * This will also request missing items from the corporea network.
   * @return if the crafting was successful (this ONLY means the craft request packet was sent to the server, it still might fail on the server side (probably because of lag))
   */
  public boolean doCraft() {
    if (currentRecipe == null) return false;
    return true;
  }

  @Nullable
  public ICraftingRecipe getCurrentRecipe() {
    return currentRecipe;
  }

  public ItemStack getCurrentOutput() {
    return currentOutput;
  }

  public ItemStack getRemainingItem(int slot) {
    if (currentRemainingItems == null) return null;
    return currentRemainingItems.get(slot);
  }

  public boolean isPointingAtInterface() {
    return Math.abs(pointingLocalPos.x) < 1 && Math.abs(pointingLocalPos.y) < 1;
  }

  @Nullable
  public CraftingInterfaceSlot getPointingSlot() {
    for (CraftingInterfaceSlot slot : slots) {
      if (slot.isPointIn(pointingLocalPos))
        return slot;
    }
    return null;
  }

  public void setTargetRotation(double rotation) {
    this.targetRotation = rotation;
  }

  public void setPos(double pos) {
    this.pos = pos;
  }

  public void setSize(double size) {
    this.size = size;
  }

  private Vec2d toLocalPos(Vec2d worldPos2d) {
    Vector3 yAxis = new Vector3(new Vector3d(0, 0, 1).rotateYaw((float) (-rotation - Math.PI)));
    Vector3 xAxis = yAxis.yCrossProduct();
    Vector3 worldPos = worldPos2d.toVector3();
    double xDist = MathUtil.pointToOrgLineDistance(yAxis, worldPos);
    double yDist = MathUtil.pointToOrgLineDistance(xAxis, worldPos);
    return new Vec2d(xDist * Math.signum(worldPos.dotProduct(xAxis)) / size, (yDist * -Math.signum(worldPos.dotProduct(yAxis)) - (pos - size)) / size - 1);
  }
}
