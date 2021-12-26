package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.PacketRequestItemListUpdate;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.Vec2f;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.mixin.AccessorRenderState;

import java.util.List;

public class RequestingHaloInterface {
  private static final Minecraft mc = Minecraft.getInstance();

  private final CISlotPointer slot;
  private final ItemStack haloItem;

  private boolean opening = true;
  private boolean closing = false;
  private float openCloseProgress = 0F;
  private boolean isNormalClose = false;

  private int tick = 0;

  private float rotationOffset;

  private final AnimatedCorporeaItemList itemList = new AnimatedCorporeaItemList();

  public RequestingHaloInterface(CISlotPointer slot) {
    this.slot = slot;
    this.haloItem = slot.getStack(mc.player);
    this.rotationOffset = mc.player.rotationYaw;
  }

  public CISlotPointer getSlot() {
    return slot;
  }

  public ItemStack getHaloItem() {
    return haloItem;
  }

  public boolean render(MatrixStack ms, float pt) {
    if (!updateOpenClose()) {
      close();
      return false;
    }

    Minecraft mc = Minecraft.getInstance();
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();

    double renderPosX = mc.getRenderManager().info.getProjectedView().getX();
    double renderPosY = mc.getRenderManager().info.getProjectedView().getY();
    double renderPosZ = mc.getRenderManager().info.getProjectedView().getZ();

    ms.push();

    PlayerEntity player = mc.player;
    double posX = player.prevPosX + (player.getPosX() - player.prevPosX) * pt;
    double posY = player.prevPosY + (player.getPosY() - player.prevPosY) * pt + player.getEyeHeight();
    double posZ = player.prevPosZ + (player.getPosZ() - player.prevPosZ) * pt;

    ms.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);

    ModRenderTypes.requestingHaloBackground = RenderType.makeType(
        IC.MODID + "_requesting_halo_bg",
        DefaultVertexFormats.POSITION_COLOR,
        GL11.GL_QUAD_STRIP,
        64, false, false,
        RenderType.State.getBuilder()
            .cull(new RenderState.CullState(false))
            .transparency(AccessorRenderState.getTranslucentTransparency())
            .build(false)
    );

    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, -mc.player.rotationYaw, true));
    IVertexBuilder buffer = buffers.getBuffer(ModRenderTypes.requestingHaloBackground);
    float progress = (float) Math.sin((Math.PI / 2F) * openCloseProgress);

    float width = (float) (progress * (Math.PI * 0.25F));
    float height = 1F;
    for (float angle = -width; angle < width; angle += Math.PI / 360F) {
      float xp = (float) Math.sin(angle) * 2F;
      float zp = (float) Math.cos(angle) * 2F;
      Matrix4f mat = ms.getLast().getMatrix();
      float alpha = progress * .6F;

      float fadeWidth = 0.3F;
      float minDistToEdge = Math.min(
          Math.abs(width - angle),
          Math.abs(-width - angle)
      );
      if (minDistToEdge < fadeWidth) {
        alpha *= Math.sin((minDistToEdge / fadeWidth) * (Math.PI / 2F));
      }

      buffer.pos(mat, xp, -height * progress, zp).color(0F, .7F, 1F, alpha).endVertex();
      buffer.pos(mat, xp, height * progress, zp).color(0F, .7F, 1F, alpha).endVertex();
    }
    ms.pop();

    itemList.update(ClientTickHandler.delta);
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, -rotationOffset, true));
    for (AnimatedItemStack aniStack : itemList.getAnimatedList()) {
      ms.push();
      Vec2f pos = aniStack.getPos();
      ms.rotate(new Quaternion(Vector3f.YP, -pos.x * 0.3F, false));
      ms.translate(0F, -(pos.y - 2) * (1F / (itemList.getHeight() + 1)) * 2F, 1.8F);
      float scale = 1F / (itemList.getHeight() + 1) * height * 2F;
      ms.scale(scale, scale, scale);
      float pp = ClientTickHandler.total * .025F;
      float mp = (1F / itemList.getHeight()) * 0.5F;

      ms.translate(
          aniStack.noise.perlin(pp, 0.0, 0.0) * mp,
          aniStack.noise.perlin(0.0, pp, 0.0) * mp,
          aniStack.noise.perlin(0.0, 0.0, pp) * mp
      );
      aniStack.renderItem(ms);
      ms.pop();
    }
    ms.pop();

    ms.pop();
    buffers.finish();

    return true;
  }

  public void tick() {
    if (tick % 20 == 0) {
      requestItemListUpdate();
    }
    tick++;
  }

  private void handleRotation() {

  }

  public boolean isClosing() {
    return closing;
  }

  private boolean updateOpenClose() {
    float animationSpeed = 10F; // How many ticks it takes to complete the open/close animation
    if (opening) {
      openCloseProgress += ClientTickHandler.delta / animationSpeed;
      if (openCloseProgress >= 1F) {
        openCloseProgress = 1F;
        opening = false;
      }
    } else if (closing) {
      openCloseProgress -= ClientTickHandler.delta / animationSpeed;
      if (openCloseProgress <= 0F) {
        openCloseProgress = 0F;
        closing = false;
        return false;
      }
    }
    return true;
  }

  public void close() {
    if (!isNormalClose) {
      RequestingHaloInterfaceHandler.resetKeyboardListener();
      mc.player.playSound(ModSounds.unholyCloak, SoundCategory.PLAYERS, 3F, 1F);
    }
  }

  /**
   * Start the close animation
   */
  public void startClose() {
    if (closing) return;
    if (!opening) {
      RequestingHaloInterfaceHandler.resetKeyboardListener();
      closing = true;
      isNormalClose = true;
      itemList.removeAll();
      mc.player.playSound(ModSounds.unholyCloak, SoundCategory.PLAYERS, 1F, 1F);
    }
  }

  private static void playSwingAnimation() {
    mc.player.swingProgressInt = -1;
    mc.player.isSwingInProgress = true;
    mc.player.swingingHand = Hand.MAIN_HAND;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseInput(int button, int action, int mods) {
    if (action == GLFW.GLFW_PRESS) {
      switch (button) {
        case GLFW.GLFW_MOUSE_BUTTON_LEFT:
          playSwingAnimation();
          break;
        case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
          playSwingAnimation();
          break;
      }
    }
    return true;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseScroll(double delta, boolean rightDown, boolean midDown, boolean leftDown) {
    return true;
  }

  public void onKeyEvent(int key, int scanCode, int action, int modifiers) {

  }

  public void onCharEvent(int codePoint, int modifiers) {

  }

  public void handleUpdatePacket(List<ItemStack> newList) {
    itemList.handleUpdatePacket(newList);
  }

  public void requestItemListUpdate() {
    ModPacketHandler.sendToServer(new PacketRequestItemListUpdate(slot));
  }
}
