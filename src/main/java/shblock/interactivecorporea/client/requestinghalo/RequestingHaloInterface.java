package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.*;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.settings.KeyConflictContext;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.requestinghalo.crafting.CraftingInterfaceSlot;
import shblock.interactivecorporea.client.requestinghalo.crafting.HaloCraftingInterface;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.CPacketRequestItem;
import shblock.interactivecorporea.common.network.CPacketRequestItemListUpdate;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.Ray3;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.Vector3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

@SuppressWarnings("ConstantConditions")
public class RequestingHaloInterface {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final IRenderTypeBuffer.Impl TEXT_BUFFERS = IRenderTypeBuffer.getImpl(new BufferBuilder(64));

  public static final KeyBinding KEY_SEARCH = new KeyBinding(
      "key." + IC.MODID + ".requesting_halo.search",
      KeyConflictContext.IN_GAME,
      InputMappings.Type.KEYSYM.getOrMakeInput(GLFW_KEY_TAB),
      IC.KEY_CATEGORY
  );

  public static final KeyBinding KEY_REQUEST_UPDATE = new KeyBinding(
      "key." + IC.MODID + ".requesting_halo.request_update",
      KeyConflictContext.IN_GAME,
      InputMappings.Type.KEYSYM.getOrMakeInput(GLFW_KEY_U),
      IC.KEY_CATEGORY
  );

  private final CISlotPointer slot;
  private ItemStack haloItem;

  private boolean opening = true;
  private boolean closing = false;
  private double openCloseProgress = 0F;
  private boolean isNormalClose = false;

  private int tick = 0;

  private static final double INITIAL_ROTATION = 36;
  private double rotationOffset;
  private double relativeRotation = INITIAL_ROTATION;
  private double lastRelativeRotation = INITIAL_ROTATION;

  private double radius = 2F;
  private double height = 1F;

  private final HaloSearchBar searchBar = new HaloSearchBar();
  private final HaloCraftingInterface craftingInterface;

  private final AnimatedCorporeaItemList itemList;
  private final AnimatedItemSelectionBox selectionBox = new AnimatedItemSelectionBox(() -> playSound(ModSounds.haloSelect, .25F, 1F));
  private HaloPickedItem pickedItem = null;
  // if the picked item should fade away when the player looks up
  private boolean shouldPickedItemFadeWhenLookUp = false;
  // when unselect the picked item or picking another one, the old one will be put in this list to display the fading animation
  private final List<HaloPickedItem> fadingPickedItems = new ArrayList<>();

  // these variables are just here to avoid multiple unnecessary calculations of them in one frame
  private double itemSpacing;
  private double itemRotSpacing;
  private double itemZOffset;
  private Vector3 bottomIntersect = new Vector3(0, 0, 0);

  private static final String PREFIX_LIST_HEIGHT = "settings_item_list_height";
  private static final String PREFIX_SEARCH_STRING = "settings_search_string";

  public RequestingHaloInterface(CISlotPointer slot) {
    this.slot = slot;
    this.haloItem = slot.getStack(mc.player);
    this.rotationOffset = mc.player.rotationYaw - INITIAL_ROTATION;
    searchBar.setUpdateCallback(this::updateSearch);

    itemList = new AnimatedCorporeaItemList(ItemNBTHelper.getInt(haloItem, PREFIX_LIST_HEIGHT, 5));

    searchBar.setSearchString(ItemNBTHelper.getString(haloItem, PREFIX_SEARCH_STRING, ""));
    searchBar.moveToEnd();

    if (isModuleInstalled(HaloModule.AMOUNT_SORT)) {
      itemList.setSortMode(SortMode.AMOUNT); //TODO: change this when adding new sort modes
    }

    craftingInterface = new HaloCraftingInterface(slot, haloItem);
    craftingInterface.setTargetRotation(Math.toRadians(INITIAL_ROTATION));
  }

  public CISlotPointer getSlot() {
    return slot;
  }

  public ItemStack getHaloItem() {
    return haloItem;
  }

  /**
   * DO NOT USE THIS! This is only used to sync the stack when updating player inventory
   */
  public void updateHaloItem(ItemStack haloItem) {
    this.haloItem = haloItem;
    craftingInterface.haloStack = haloItem;
  }

  public boolean isModuleInstalled(HaloModule module) {
    return ItemRequestingHalo.isModuleInstalled(haloItem, module);
  }

  public boolean render(MatrixStack ms, double pt) {
    if (!updateOpenClose()) {
      close();
      return false;
    }

//    RenderTick.pt = (float) pt;
//    try {
//      Method method = ClientTickHandler.class.getDeclaredMethod("calcDelta");
//      method.setAccessible(true);
//      method.invoke(ClientTickHandler.class);
//      method.setAccessible(false);
//    } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
//      e.printStackTrace();
//    }
    //TODO: fix delta calculation
//    System.out.println(RenderTick.delta);

    handleRotation();

    ActiveRenderInfo info = mc.getRenderManager().info;
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();

    double renderPosX = info.getProjectedView().getX();
    double renderPosY = info.getProjectedView().getY();
    double renderPosZ = info.getProjectedView().getZ();

    ms.push();

    PlayerEntity player = mc.player;
    Vector3d eyePos = player.getEyePosition((float) pt);

    ms.translate(eyePos.x - renderPosX, eyePos.y - renderPosY, eyePos.z - renderPosZ);

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

    itemList.update(RenderTick.delta);
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) -rotationOffset, true));
    double scale = 1D / itemList.getHeight() * height * 2D;
    itemSpacing = (1D / itemList.getHeight()) * 2D * height;
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

      double pp = RenderTick.total * .025;
      double mp = 1 / currentScale * 0.0375;
      ms.translate(
          aniStack.noise.perlin(pp, 0, 0) * mp,
          aniStack.noise.perlin(0, pp, 0) * mp,
          0F
      );

      aniStack.renderItem(ms);

      ms.push();
      float ts = 1F / 24F;
      ms.scale(ts, ts, ts);
      ms.translate(-itemSpacing - 10D, -itemSpacing - 4D, -0.02);
      aniStack.renderAmount(ms, 0x00FFFFFF | 0xFF << 24, TEXT_BUFFERS);
      ms.pop();

      ms.push();
      ts = 1F / 18F;
      ms.scale(ts, ts, ts);
      ms.translate(0F, 0F, -0.05);
      aniStack.renderRequestResultAnimations(ms, buffers);
      ms.pop();

      ms.pop();
    }
    if (!haveSelectedItem || closing) {
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

    if (isModuleInstalled(HaloModule.SEARCH)) {
      renderSearchBar(ms, pt);
    }

    renderPickedItem(ms);

    if (isModuleInstalled(HaloModule.CRAFTING)) {
      renderCrafting(ms, pt);
    }

    ms.pop();

    return true;
  }

  public void renderSearchBar(MatrixStack ms, double pt) {
    ms.push();
    ms.rotate(new Quaternion(Vector3f.YP, (float) (-rotationOffset - relativeRotation), true));
    ms.translate(0, 0, radius);
    ms.scale((float) Math.sin(openCloseProgress * Math.PI * .5), 1, (float) Math.max(Math.sin((openCloseProgress - .5) * Math.PI), .01));
    ms.translate(0, 0, -radius);
    searchBar.render(ms, radius, height);
    ms.pop();
  }

  public void renderCrafting(MatrixStack ms, double pt) {
    double size = .5;
    craftingInterface.setSize(size);
    craftingInterface.setPos(MathUtil.calcChordCenterDistance(radius, size * 2) - size - .1);

    double lockingRotation = 90;
    double relPlus = relativeRotation - INITIAL_ROTATION;
    double lastRelPlus = lastRelativeRotation - INITIAL_ROTATION;
    if (Math.floor(relPlus / lockingRotation) != Math.floor(lastRelPlus / lockingRotation)) {
      double relRot = Math.max(relPlus, lastRelPlus);
      double rotDegrees = Math.floor(relRot / lockingRotation) * lockingRotation + INITIAL_ROTATION;
      craftingInterface.setTargetRotation(Math.toRadians(rotDegrees));
    }

    ms.push();
    ms.translate(0, -height, 0);
    ms.rotate(Vector3f.YP.rotationDegrees((float) -rotationOffset));
    craftingInterface.render(ms, Math.sin(openCloseProgress * Math.PI / 2));
    ms.pop();
  }

  public void renderPickedItem(MatrixStack ms) {
    if (pickedItem != null) {
      if (pickedItem.render(ms)) {
        pickedItem = null;
      }
    }
    for (int i = fadingPickedItems.size() - 1; i >= 0; i--) {
      if (fadingPickedItems.get(i).render(ms))
        fadingPickedItems.remove(i);
    }
  }

  public void renderHud(MatrixStack ms, float pt, MainWindow window) {
    if (isModuleInstalled(HaloModule.HUD)) {
      ms.push();
      AnimatedItemStack aniStack = selectionBox.getTarget();
      if (aniStack != null) {
        aniStack.renderHud(ms, pt, window);
      }
      ms.pop();
    }
  }

  public void tick() {
    drainManaOrClose(ModConfig.COMMON.requestingHaloStaticConsumption.get());

    itemList.tick();
    if (tick == 0) {
      requestItemListUpdate();
    } else if (tick % 20 == 0) {
      if (isModuleInstalled(HaloModule.UPDATE)) {
        requestItemListUpdate();
      }
    }

    Vector3 lookDir = new Vector3(Vector3d.fromPitchYaw(mc.player.rotationPitch, mc.player.rotationYaw));
    bottomIntersect = MathUtil.rayPlaneIntersection(
        new Ray3(new Vector3(0, 0, 0), lookDir),
        new Ray3(new Vector3(0, -height, 0), new Vector3(0, 1, 0))
    );
    if (pickedItem != null) {
      if (bottomIntersect != null && new Vector3(bottomIntersect.x, 0, bottomIntersect.z).mag() < radius * .94) {
        shouldPickedItemFadeWhenLookUp = true;
      }
      if (shouldPickedItemFadeWhenLookUp && Math.tan(Math.toRadians(mc.player.rotationPitch)) * radius < height - .05) {
        pickedItem.fadeOut();
        shouldPickedItemFadeWhenLookUp = false;
      }

      if (bottomIntersect != null && new Vector3(bottomIntersect.x, 0, bottomIntersect.z).mag() < radius) {
        pickedItem.setTargetPosition(bottomIntersect.add(0, .05, 0));
        pickedItem.setTargetRotationDegrees(-90, -mc.player.rotationYaw + 180);
      } else {
        pickedItem.setTargetPosition(lookDir.multiply(radius * .8));
        pickedItem.setTargetRotationDegrees(-mc.player.rotationPitch, -mc.player.rotationYaw + 180);
      }
      pickedItem.tick();
    }
    fadingPickedItems.forEach(HaloPickedItem::tick);

    craftingInterface.tick(bottomIntersect != null ? new Vec2d(bottomIntersect.rotate(Math.toRadians(rotationOffset), new Vector3(0, 1, 0))) : null);

    tick++;
  }

  /**
   * @return the rotation (in radians) needed to reach the end of the item list
   */
  private double getItemListDisplayWidth() {
    int itemCnt = itemList.getAnimatedList().size();
    int cols = itemCnt / itemList.getHeight();
    if (itemCnt % itemList.getHeight() != 0)
      cols++;
    return (cols - 1) * itemRotSpacing;
  }

  private double prevPlayerRot = mc.player.rotationYaw;
  private double rotationToAdd = 0;
  private double rotationAddingSpeed = 0;
  public void handleRotation() {
    lastRelativeRotation = relativeRotation;

    double rot = mc.player.rotationYaw;
    double ra = rot - prevPlayerRot;
    double rb = rot - prevPlayerRot;
    if (Math.abs(ra) < Math.abs(rb)) {
      relativeRotation += ra;
    } else {
      relativeRotation += rb;
    }
    prevPlayerRot = rot;

    double dt = RenderTick.delta;
    rotationAddingSpeed = MathUtil.smoothMovingSpeed(rotationToAdd, 0, rotationAddingSpeed, .3, .7, .01);
    rotationToAdd += rotationAddingSpeed * dt;
    rotationOffset += rotationAddingSpeed * dt;
    relativeRotation -= rotationAddingSpeed * dt;

    limitPlayerRotation();
  }

  private boolean lastLimitRotation = false;
  private void limitPlayerRotation() {
    if (isOpenClose()) return;
    if (itemList.getAnimatedList().size() == 0) {
      lastLimitRotation = false;
      return;
    }

    if (Math.abs(Math.toRadians(mc.player.rotationPitch)) > Math.atan(height / radius)) {
      lastLimitRotation = false;
      return;
    }

    double excessSpacing = 3;
    double correctionSpd = .1;
    double minSpd = .1;
    double degreeItemRotSpacing = Math.toDegrees(itemRotSpacing);

    boolean limited = false;

    double start = (-degreeItemRotSpacing / 2 - excessSpacing);
    double distToStart = relativeRotation - start;
    if (distToStart < 0) {
      mc.player.rotationYaw = (float) (mc.player.rotationYaw - (distToStart * correctionSpd - minSpd) * RenderTick.delta);
      spawnParticleLineOnHalo(start);
      limited = true;
    }

    double end = (Math.toDegrees(getItemListDisplayWidth()) + degreeItemRotSpacing / 2 + excessSpacing);
    double distToEnd = relativeRotation - end;
    if (distToEnd > 0) {
      mc.player.rotationYaw = (float) (mc.player.rotationYaw - (distToEnd * correctionSpd + minSpd) * RenderTick.delta);
      spawnParticleLineOnHalo(end);
      limited = true;
    }

    if (limited && !lastLimitRotation) {
//      playSound(ModSounds.haloReachEdge, 1F); //TODO: add this back when finds a better sound effect
    }

    lastLimitRotation = limited;
  }

  private float particleSpawnTimer = 0F;
  private void spawnParticleLineOnHalo(double relativeRot) {
    Vector3d mid = new Vector3d(radius * .95, 0, 0);
    mid = mid.rotateYaw((float) Math.toRadians(-rotationOffset - 90 - relativeRot));
    mid = mid.add(mc.player.getEyePosition((float) RenderTick.pt));
    particleSpawnTimer += RenderTick.delta;
    Random random = new Random();
    int cnt = 0;
    for (int i = 0; i < (int) (particleSpawnTimer / .1); i++) {
      Vector3d pos = mid.add(0, (random.nextDouble() * 2 - 1) * height, 0);
      mc.world.addParticle(new RedstoneParticleData(1F, 0F, 0F, 1F), pos.x, pos.y, pos.z, 0, 0, 0);
      cnt++;
    }
    particleSpawnTimer -= cnt * .1;
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

  private boolean updateOpenClose() {
    double animationSpeed = 10F; // How many ticks it takes to complete the open/close animation
    if (opening) {
      openCloseProgress += RenderTick.delta / animationSpeed;
      if (openCloseProgress >= 1F) {
        openCloseProgress = 1F;
        opening = false;
      }
    } else if (closing) {
      openCloseProgress -= RenderTick.delta / animationSpeed;
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
      playSound(ModSounds.haloClose, 1F);
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
      playSound(ModSounds.haloClose, 1F);

      if (pickedItem != null) {
        pickedItem.fadeOut();
        fadingPickedItems.add(pickedItem);
        pickedItem = null;
      }
    }
  }

  private static void playSwingAnimation() {
    mc.player.swingProgressInt = -1;
    mc.player.isSwingInProgress = true;
    mc.player.swingingHand = Hand.MAIN_HAND;
  }

  public boolean requestItem() {
    if (isOpenClose()) return false;
    if (!isModuleInstalled(HaloModule.RECEIVE)) return false;
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
    Vector3 normal = new Vector3(Math.sin(rot) * itemZOffset * .9, 0, Math.cos(rot) * itemZOffset * .9);
    Vector3 pos = normal.add(player.getPosX(), player.getPosYEye() - Math.tan(Math.toRadians(mc.player.rotationPitch)) * radius, player.getPosZ());
    ModPacketHandler.sendToServer(new CPacketRequestItem(slot, reqStack, pos, normal, itemList.onRequest(aniStack)));
    playSwingAnimation();
    selectionBox.playRequestAnimation();
    playSound(pos.x, pos.y, pos.z, ModSounds.haloRequest, 1F);
    return true;
  }

  private boolean drainManaOrClose(int amount) {
    if (isOpenClose()) return true;
    ManaItemHandler manaItemHandler = ManaItemHandler.instance();
    if (!manaItemHandler.requestManaExactForTool(haloItem, mc.player, amount, true)) {
      startClose();
      return false;
    }
    return true;
  }

  private void updateSearch() {
    itemList.setFilter(searchBar.getSearchString());
    itemList.arrange();
    ItemNBTHelper.setString(haloItem, PREFIX_SEARCH_STRING, searchBar.getSearchString());
  }

  private boolean pickItem(boolean unpick) {
    if (unpick) {
      if (pickedItem == null) return false;

      pickedItem.fadeOut();
      fadingPickedItems.add(pickedItem);
      pickedItem = null;
    } else {
      HaloPickedItem newPickedItem = null;
      if (mc.currentScreen == null) {
        if (selectionBox.getTarget() != null) {
          double rot = Math.toRadians(-rotationOffset - relativeRotation);
          double pitch = Math.toRadians(mc.player.rotationPitch);
          Vector3 pos = new Vector3(
              Math.sin(rot) * itemZOffset,
              -Math.tan(pitch) * radius,
              Math.cos(rot) * itemZOffset
          );
          newPickedItem = new HaloPickedItem(selectionBox.getTarget().getStack(), pos, 0, rot + Math.PI);
          playSwingAnimation();
        } else {
          CraftingInterfaceSlot slot = craftingInterface.getPointingSlot();
          if (slot != null && !slot.getShadowStack().isEmpty() && bottomIntersect != null) {
            newPickedItem = new HaloPickedItem(slot.getShadowStack(), bottomIntersect.add(0, .05, 0), -Math.PI / 4, Math.toRadians(-mc.player.rotationYaw + 180));
          }
        }
      } else {
        ItemStack stackUnderMouse = RequestingHaloInterfaceHandler.getUnderMouseItemStack();
        if (!stackUnderMouse.isEmpty()) {
          newPickedItem = new HaloPickedItem(stackUnderMouse, new Vector3(0, 0, 0), Math.toRadians(-mc.player.rotationPitch), Math.toRadians(-mc.player.rotationYaw + 180));
        }
      }

      if (newPickedItem == null) return false;

      if (pickedItem != null) {
        pickedItem.fadeOut();
        fadingPickedItems.add(pickedItem);
      }
      pickedItem = newPickedItem;
    }
    return true;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseInput(int button, int action, int mods) {
    if (isOpenClose()) return false;

    Vector3 intersectWorldPos = null;
    if (bottomIntersect != null)
      intersectWorldPos = bottomIntersect.add(new Vector3(mc.player.getEyePosition((float) RenderTick.pt)));
    if (action == GLFW_PRESS) {
      switch (button) {
        case GLFW_MOUSE_BUTTON_LEFT:
          if (mc.currentScreen != null) return false;
          if (requestItem())
            return true;
          if (craftingInterface != null) {
            if (craftingInterface.handleSlotInteraction(false, intersectWorldPos, pickedItem)) {
              playSwingAnimation();
              return true;
            }
          }
          break;
        case GLFW_MOUSE_BUTTON_RIGHT:
          if (mc.currentScreen != null) return false;
          if (craftingInterface != null) {
            if (KeyboardHelper.hasShiftDown()) {
              if (craftingInterface != null) {
                craftingInterface.tryOpenJei();
                return true;
              }
            } else {
              if (craftingInterface.handleSlotInteraction(true, intersectWorldPos, pickedItem)) {
                playSwingAnimation();
                return true;
              }
            }
          }
          break;
        case GLFW_MOUSE_BUTTON_MIDDLE:
          if (pickItem(KeyboardHelper.hasShiftDown()))
            return true;
          break;
      }
    } else {
      return false;
    }
    if (craftingInterface.isPointingAtInterface())
      return true;
    return false;
  }

  /**
   * @return If the action is consumed
   */
  public boolean onMouseScroll(double delta, boolean rightDown, boolean midDown, boolean leftDown) {
    if (isOpenClose()) return false;

    if (KeyboardHelper.hasControlDown()) {
      itemList.changeHeight((int) -delta);
      ItemNBTHelper.setInt(haloItem, PREFIX_LIST_HEIGHT, itemList.getHeight());
      itemList.arrange();
      return true;
    }
    if (KeyboardHelper.hasAltDown()) {
      rotationToAdd += delta * Math.toDegrees(itemRotSpacing);
      return true;
    }
    return false;
  }

  public void preKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (isOpenClose()) return;

    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
      if (Screen.isCopy(key)) {
        searchBar.copy();
      } else if (Screen.isPaste(key)) {
        searchBar.paste();
      } else if (Screen.isCut(key)) {
        searchBar.cut();
      } else if (Screen.isSelectAll(key)) {
        searchBar.selectAll();
      }
    }
  }

  private static final Int2IntFunction craftingSlotKeyMap = key -> {
    switch (key) {
      case GLFW_KEY_KP_7: return 0;
      case GLFW_KEY_KP_8: return 1;
      case GLFW_KEY_KP_9: return 2;
      case GLFW_KEY_KP_4: return 3;
      case GLFW_KEY_KP_5: return 4;
      case GLFW_KEY_KP_6: return 5;
      case GLFW_KEY_KP_1: return 6;
      case GLFW_KEY_KP_2: return 7;
      case GLFW_KEY_KP_3: return 8;
    }
    return -1;
  };
  public void onKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (isOpenClose()) return;

    if (mc.currentScreen == null) {
    if (isModuleInstalled(HaloModule.SEARCH)) {
      if (KEY_SEARCH.isPressed()) {
//        if (KeyboardHelper.hasShiftDown()) {
//          if (craftingInterface != null) {
//            craftingInterface.tryOpenJei();
//            return;
//          }
//        } else {
          searchBar.setSearching(!searchBar.isSearching());
          return;
//        }
      }
    }
    if (!isModuleInstalled(HaloModule.UPDATE)) {
      if (KEY_REQUEST_UPDATE.isPressed()) {
        requestItemListUpdate();
        return;
      }
    }
    if (action == GLFW_PRESS || action == GLFW_REPEAT) {
        if (searchBar.isSearching()) {
          switch (key) {
            case GLFW_KEY_BACKSPACE:
              searchBar.backspace();
              break;
            case GLFW_KEY_DELETE:
              searchBar.delete();
              break;
            case GLFW_KEY_LEFT:
              searchBar.moveSelectionPos(-1, !KeyboardHelper.hasShiftDown());
              break;
            case GLFW_KEY_RIGHT:
              searchBar.moveSelectionPos(1, !KeyboardHelper.hasShiftDown());
              break;
            case GLFW_KEY_HOME:
              searchBar.moveToStart();
              break;
            case GLFW_KEY_END:
              searchBar.moveToEnd();
              break;
          }
        } else {
          if (key == GLFW_KEY_ENTER) {
            craftingInterface.doCraft();
          }
        }
      }
    } else {
      if (action == GLFW_PRESS) {
        int slot = craftingSlotKeyMap.applyAsInt(key);
        if (slot != -1) {
          ItemStack stackUnderMouse = RequestingHaloInterfaceHandler.getUnderMouseItemStack();
          if (!stackUnderMouse.isEmpty()) {
            craftingInterface.getPointingSlot().setShadowStack(stackUnderMouse);
            return;
          }
        }
      }
    }
  }

  public boolean shouldCancelKeyEvent(int key, int scanCode) {
    if (isOpenClose()) return false;
    if (!searchBar.isSearching() && RequestingHaloInterfaceHandler.KEY_BINDING.matchesKey(key, scanCode)) return false;
    if (KEY_SEARCH.matchesKey(key, scanCode))
      return false;
    switch (key) {
      case GLFW_KEY_BACKSPACE:
      case GLFW_KEY_DELETE:
      case GLFW_KEY_LEFT:
      case GLFW_KEY_RIGHT:
      case GLFW_KEY_LEFT_SHIFT:
      case GLFW_KEY_RIGHT_SHIFT:
      case GLFW_KEY_HOME:
      case GLFW_KEY_END:
        return false;
    }
    return searchBar.isSearching();
  }

  public void onCharEvent(int codePoint, int modifiers) {
    if (isOpenClose()) return;
    searchBar.typeChar(codePoint, modifiers);
  }

  public void handleUpdatePacket(List<ItemStack> newList) {
    if (drainManaOrClose(ModConfig.COMMON.requestingHaloUpdateConsumption.get())) {
      itemList.handleUpdatePacket(newList);

      if (!isOpenClose()) {
        if (!ItemRequestingHalo.isModuleInstalled(haloItem, HaloModule.UPDATE)) {
          playSound(ModSounds.haloListUpdate, 1F);
        }
      }
    }
  }

  public void handleRequestResultPacket(int requestId, int successAmount) {
    itemList.handleRequestResultPacket(requestId, successAmount);
  }

  private int lastRequestTick = 0;

  public void requestItemListUpdate() {
    if (tick - lastRequestTick >= 5 || tick == 0) {
      ModPacketHandler.sendToServer(new CPacketRequestItemListUpdate(slot));
      lastRequestTick = tick;
    }
  }

  public boolean isOpenClose() {
    return opening || closing;
  }

  public void playSound(double x, double y, double z, SoundEvent sound, float volume, float pitch) {
    if (mc.world != null) {
      ClientWorld world = mc.world;
      world.playSound(x, y, z, sound, SoundCategory.PLAYERS, volume, pitch, false);
    }
  }

  public void playSound(double x, double y, double z, SoundEvent sound, float pitch) {
    playSound(x, y, z, sound, 1F, pitch);
  }

  public void playSound(SoundEvent sound, float volume, float pitch) {
    playSound(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), sound, volume, pitch);
  }

  public void playSound(SoundEvent sound, float pitch) {
    playSound(sound, 1F, pitch);
  }

  public HaloCraftingInterface getCraftingInterface() {
    return craftingInterface;
  }
}
