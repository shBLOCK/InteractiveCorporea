package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.render.ModRenderTypes;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.client.util.MathUtil;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.PacketRequestItem;
import shblock.interactivecorporea.common.network.PacketRequestItemListUpdate;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.mixin.AccessorRenderState;

import java.util.List;

@SuppressWarnings("ConstantConditions")
public class RequestingHaloInterface {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final IRenderTypeBuffer.Impl TEXT_BUFFERS = IRenderTypeBuffer.getImpl(new BufferBuilder(64));

  public static final KeyBinding KEY_RESET_ROTATION = new KeyBinding(
      "key.interactive_corporea.requesting_halo.reset_rotation",
      KeyConflictContext.IN_GAME,
      KeyModifier.SHIFT,
      InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_R),
      IC.KEY_CATEGORY
  );
  public static final KeyBinding KEY_SEARCH = new KeyBinding(
      "key.interactive_corporea.requesting_halo.search",
      KeyConflictContext.IN_GAME,
      InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_TAB),
      IC.KEY_CATEGORY
  );

  private final CISlotPointer slot;
  private final ItemStack haloItem;

  private boolean opening = true;
  private boolean closing = false;
  private double openCloseProgress = 0F;
  private boolean isNormalClose = false;

  private int tick = 0;

  private double rotationOffset;
  private double relativeRotation;

  private double radius = 2F;
  private double height = 1F;

  private String searchString = "";
  private boolean searching = false;

  private final AnimatedCorporeaItemList itemList = new AnimatedCorporeaItemList();
  private final AnimatedItemSelectionBox selectionBox = new AnimatedItemSelectionBox();

  // these variables are just here to avoid multiple unnecessary calculations of them in one frame
  private double itemSpacing;
  private double itemRotSpacing;
  private double itemZOffset;

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

  public boolean render(MatrixStack ms, double pt) {
    if (!updateOpenClose()) {
      close();
      return false;
    }

    handleRotation();

    ActiveRenderInfo info = mc.getRenderManager().info;
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();

    double renderPosX = info.getProjectedView().getX();
    double renderPosY = info.getProjectedView().getY();
    double renderPosZ = info.getProjectedView().getZ();

    ms.push();

    PlayerEntity player = mc.player;
    double posX = player.prevPosX + (player.getPosX() - player.prevPosX) * pt;
    double posY = player.prevPosY + (player.getPosY() - player.prevPosY) * pt + mc.player.getEyeHeight();
    double posZ = player.prevPosZ + (player.getPosZ() - player.prevPosZ) * pt;

    ms.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);

    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
    double progress = Math.sin((Math.PI / 2F) * openCloseProgress);
    double fadeWidth = .3;
    double width = progress * (Math.PI * 0.25F);
    RenderUtil.renderPartialHalo(
        ms,
        radius,
        width - fadeWidth,
        height * progress + .05,
        fadeWidth,
        0F, .7F, 1F,
        (float) (progress * .6F)
    );
    ms.pop();

    double fadeDegrees = Math.toDegrees(fadeWidth);
    double widthDegrees = Math.toDegrees(width);

    itemList.update(ClientTickHandler.delta);
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
    double scale = 1D / itemList.getHeight() * height * 2D;
    itemSpacing = (1D / itemList.getHeight()) * 2D;
    itemRotSpacing = MathUtil.calcRadiansFromChord(radius, itemSpacing);
    itemZOffset = MathUtil.calcChordCenterDistance(radius, itemSpacing);
    Vec2d lookingPos = calcLookingPos(radius, itemSpacing, itemRotSpacing);
    boolean haveSelectedItem = false;
    for (AnimatedItemStack aniStack : itemList.getAnimatedList()) {
      if (updateSelectionBox(aniStack, lookingPos)) {
        haveSelectedItem = true;
      }

      Vec2d pos = aniStack.getPos();
      float rot = (float) (pos.x * itemRotSpacing);
      double degreeDiff = Math.abs(relativeRotation - Math.toDegrees(rot));
      if (degreeDiff >= widthDegrees) {
        continue;
      }

      float currentScale = (float) (scale * Math.sin(MathHelper.clamp(widthDegrees - degreeDiff, 0F, fadeDegrees) / fadeDegrees * Math.PI * .5F));
      ms.push();
      ms.rotate(new Quaternion(Vector3f.YP, -rot, false));
      ms.translate(0F, -(pos.y - (itemList.getHeight() - 1D) / 2D) * itemSpacing, itemZOffset);
      ms.scale(currentScale, currentScale, currentScale);

      double pp = ClientTickHandler.total * .025;
      double mp = 1 / currentScale * 0.0375;
      ms.translate(
          aniStack.noise.perlin(pp, 0, 0) * mp,
          aniStack.noise.perlin(0, pp, 0) * mp,
          0F
      );

      ms.push();
      ms.scale(1F, 1F, 0.001F);
      ms.rotate(Vector3f.YP.rotationDegrees(180));
      aniStack.renderItem(ms, buffers);
      ms.pop();
      buffers.finish();

      ms.push();
      float ts = 1F / 24F;
      ms.scale(ts, ts, ts);
      ms.translate(-itemSpacing - 10D, -itemSpacing - 4D, -0.02);
      aniStack.renderAmount(ms, 0xFFFFFFFF, TEXT_BUFFERS);
      ms.pop();

      ms.pop();
    }
    if (!haveSelectedItem) {
      selectionBox.setTarget(null);
    }
    ms.pop();
    TEXT_BUFFERS.finish();

    selectionBox.update();

    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
    Vec2d selPos = selectionBox.getPos();
    ms.rotate(Vector3f.YP.rotation((float) (-selPos.x * itemRotSpacing)));
    ms.translate(0F, -(selPos.y - (itemList.getHeight() - 1) / 2F) * itemSpacing, itemZOffset);
    float s = (float) scale;
    ms.scale(s, s, s);
    selectionBox.render(ms);
    ms.pop();

    renderSearchBar(ms, pt);

    ms.pop();

    return true;
  }

  public void renderSearchBar(MatrixStack ms, double pt) {
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
    double barHeight = .1;
    ms.translate(0, height + barHeight + .1, 0);
    float r = searching ? .1F : 1F;
    float g = searching ? 1F : .1F;
    float b = .1F;
    RenderUtil.renderPartialHalo(
        ms,
        radius,
        .5F,
        barHeight,
        .1,
        r, g, b,
        .6F
    );
    RenderUtil.renderTextOnHaloCentered(ms, mc.fontRenderer, searchString, radius - .01, .02F, 0xFFFFFFFF);
    ms.pop();
  }

  public void renderHud(MatrixStack ms, float pt, MainWindow window) {
    ms.push();
    AnimatedItemStack aniStack = selectionBox.getTarget();
    if (aniStack != null) {
      aniStack.renderHud(ms, pt, window);
    }
    ms.pop();
  }

  public void tick() {
    itemList.tick();
    if (tick % 20 == 0) {
      requestItemListUpdate();
    }
    tick++;
  }

  private double prevPlayerRot = mc.player.rotationYaw;

  public void handleRotation() {
    double rot = mc.player.rotationYaw;
    double ra = rot - prevPlayerRot;
    double rb = rot - prevPlayerRot;
    if (Math.abs(ra) < Math.abs(rb)) {
      relativeRotation += ra;
    } else {
      relativeRotation += rb;
    }
    prevPlayerRot = rot;
  }

  /**
   * @param radius the radius of the halo ring (the distance from player's position to the halo surface)
   * @param rotSpacing the radians between two items
   */
  private Vec2d calcLookingPos(double radius, double spacing, double rotSpacing) {
    return new Vec2d(
        (Math.toRadians(relativeRotation) / rotSpacing),
        (Math.tan(Math.toRadians(mc.player.rotationPitch)) * radius / spacing) + (itemList.getHeight() - 1) / 2F
    );
  }

  private boolean isLookingAtItem(Vec2d itemPos, Vec2d lookingPos) {
    return Math.abs(lookingPos.x - itemPos.x) < .5F && Math.abs(lookingPos.y - itemPos.y) < .5F;
  }

  /**
   * called for EVERY rendering item
   */
  private boolean updateSelectionBox(AnimatedItemStack stack, Vec2d lookingPos) {
    if (stack.isRemoved()) return false;

    Vec2d itemPos = stack.getPos();
    if (isLookingAtItem(itemPos, lookingPos)) {
      selectionBox.setTarget(stack);
      return true;
    }
    return false;
  }

  public boolean isClosing() {
    return closing;
  }

  private boolean updateOpenClose() {
    double animationSpeed = 10F; // How many ticks it takes to complete the open/close animation
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
    itemList.removeAll();
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
//      itemList.removeAll();
      mc.player.playSound(ModSounds.unholyCloak, SoundCategory.PLAYERS, 1F, 1F);
    }
  }

  private static void playSwingAnimation() {
    mc.player.swingProgressInt = -1;
    mc.player.isSwingInProgress = true;
    mc.player.swingingHand = Hand.MAIN_HAND;
  }

  public boolean requestItem() {
    if (closing) return false;
    AnimatedItemStack aniStack = selectionBox.getTarget();
    if (aniStack == null || aniStack.isRemoved()) return false;
    ItemStack stack = aniStack.getStack();
    int requestCnt = 1;
    if (KeyboardHelper.hasShiftDown() && KeyboardHelper.hasControlDown()) {
      requestCnt = stack.getMaxStackSize() / 4;
    } else if (KeyboardHelper.hasControlDown()) {
      requestCnt = stack.getMaxStackSize() / 2;
    } else if (KeyboardHelper.hasShiftDown()) {
      requestCnt = stack.getMaxStackSize();
    }
    ItemStack reqStack = stack.copy();
    reqStack.setCount(requestCnt);
    PlayerEntity player = mc.player;
    double rot = Math.toRadians(-rotationOffset - relativeRotation);
    System.out.println(itemZOffset);
    Vector3 normal = new Vector3(Math.sin(rot) * itemZOffset * .9, 0, Math.cos(rot) * itemZOffset * .9);
    Vector3 pos = normal.add(player.getPosX(), player.getPosYEye() - Math.tan(Math.toRadians(mc.player.rotationPitch)) * radius, player.getPosZ());
    ModPacketHandler.sendToServer(new PacketRequestItem(slot, reqStack, pos, normal));
    playSwingAnimation();
    selectionBox.playRequestAnimation();
    return true;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseInput(int button, int action, int mods) {
    if (action == GLFW.GLFW_PRESS) {
      switch (button) {
        case GLFW.GLFW_MOUSE_BUTTON_LEFT:
          if (requestItem()) {
            return true;
          }
          break;
        case GLFW.GLFW_MOUSE_BUTTON_RIGHT:
          break;
      }
    }
    return false;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseScroll(double delta, boolean rightDown, boolean midDown, boolean leftDown) {
    if (KeyboardHelper.hasControlDown()) {
      itemList.changeHeight((int) -delta);
      itemList.arrange();
      return true;
    }
    return false;
  }

  public void onKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (KEY_RESET_ROTATION.isPressed()) {
      rotationOffset = mc.player.rotationYaw;
      relativeRotation = 0;
      //TODO: smooth animation here
    }
    if (KEY_SEARCH.isPressed()) {
      searching = !searching;
    }
    if (action == GLFW.GLFW_PRESS) {
      if (searching) {
        if (key == GLFW.GLFW_KEY_BACKSPACE) {
          if (searchString.length() > 0) {
            searchString = searchString.substring(0, searchString.length() - 1);
          }
        }
      }
    }
  }

  public boolean shouldCancelKeyEvent(int key, int scanCode) {
    if (KEY_SEARCH.matchesKey(key, scanCode) || key == GLFW.GLFW_KEY_BACKSPACE) {
      return false;
    }
    return searching;
  }

  @SuppressWarnings("StringConcatenationInLoop")
  public void onCharEvent(int codePoint, int modifiers) {
    if (!closing && !opening) {
      if (searching) {
        for (char c : Character.toChars(codePoint)) {
          System.out.println(c);
          searchString += c;
        }
      }
    }
  }

  public void handleUpdatePacket(List<ItemStack> newList) {
    itemList.handleUpdatePacket(newList);
  }

  public void requestItemListUpdate() {
    ModPacketHandler.sendToServer(new PacketRequestItemListUpdate(slot));
  }
}
