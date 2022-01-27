package shblock.interactivecorporea.client.requestinghalo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.CurioSlotPointer;
import shblock.interactivecorporea.common.util.ToolItemHelper;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = IC.MODID)//TODO: advancements?
public class RequestingHaloInterfaceHandler {
  private static final Minecraft mc = Minecraft.getInstance();
  private static RequestingHaloInterface haloInterface;

  public static final KeyBinding KEY_BINDING = new KeyBinding(
      "key.interactive_corporea.requesting_halo",
      KeyConflictContext.IN_GAME,
      InputMappings.Type.KEYSYM.getOrMakeInput(GLFW.GLFW_KEY_TAB),
      IC.KEY_CATEGORY
  );

  public static RequestingHaloInterface getInterface() {
    return haloInterface;
  }

  public static void openInterface(RequestingHaloInterface face) {
    haloInterface = face;
    setupKeyboardListener();
    face.playSound(ModSounds.haloOpen, 1F);
  }

  public static boolean isInterfaceOpened() {
    return haloInterface != null;
  }

  private static void setupKeyboardListener() {
    KeyBinding.unPressAllKeys();
    GLFW.glfwSetKeyCallback(mc.getMainWindow().getHandle(),
        (windowPointer, key, scanCode, action, modifiers) -> {
          preKeyEvent(key, scanCode, action, modifiers);
          if ((!shouldCancelKeyEvent(key, scanCode)) || mc.currentScreen != null) {
            mc.execute(() -> mc.keyboardListener.onKeyEvent(windowPointer, key, scanCode, action, modifiers));
          }
        });
    GLFW.glfwSetCharModsCallback(mc.getMainWindow().getHandle(), (windowPointer, codePoint, modifiers) -> {
      if (mc.currentScreen != null) {
        mc.execute(() -> {
          mc.keyboardListener.onCharEvent(windowPointer, codePoint, modifiers);
        });
      } else {
        RequestingHaloInterfaceHandler.charCallback(codePoint, modifiers);
      }
    });
  }

  public static void resetKeyboardListener() {
    mc.keyboardListener.setupCallbacks(mc.getMainWindow().getHandle());
  }

  /**
   * Start the close animation
   */
  public static void closeInterface() {
    if (getInterface() != null) {
      getInterface().startClose();
      resetKeyboardListener();
    }
  }

  /**
   * Close the Interface immediately
   */
  public static void clearInterface() {
    if (haloInterface != null) {
      haloInterface.close();
      haloInterface = null;
      resetKeyboardListener();
    }
  }

  public static CISlotPointer getFirstHaloSlot(PlayerEntity player) {
    if (player.inventory.getCurrentItem().getItem() instanceof ItemRequestingHalo) {
      return new CISlotPointer(player.inventory.currentItem);
    }

    CurioSlotPointer cSlot = ToolItemHelper.getFirstMatchedCurioSlot(player, ItemRequestingHalo.class);
    if (cSlot != null) {
      return new CISlotPointer(cSlot);
    }

    int slot = ToolItemHelper.getFirstMatchedSlotInInventory(player, ItemRequestingHalo.class);
    if (slot != -1) {
      return new CISlotPointer(slot);
    }

    return null;
  }

  public static boolean tryOpen(PlayerEntity player) {
    CISlotPointer slot = getFirstHaloSlot(player);
    if (slot != null) {
      openInterface(new RequestingHaloInterface(slot));
      return true;
    }
    return false;
  }

  /**
   * Check if the ItemStack in the slot of current opened interface is still the original one (If the halo item has not been changed)
   */
  public static boolean slotStillValid() {
    return getInterface().getSlot().getStack(mc.player) == getInterface().getHaloItem();
  }

  public static void handleUpdatePacket(List<ItemStack> itemList) {
    if (getInterface() != null) {
      getInterface().handleUpdatePacket(itemList);
    }
  }

  public static void handleRequestResultPacket(int requestId, int successAmount) {
    if (getInterface() != null) {
      getInterface().handleRequestResultPacket(requestId, successAmount);
    }
  }

  @SubscribeEvent
  public static void onLogOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
    haloInterface = null;
  }

  @SubscribeEvent
  public static void onWorldRender(RenderWorldLastEvent event) {
    RequestingHaloInterface face = getInterface();
    if (face != null) {
      if (!slotStillValid()) {
        clearInterface();
      }
//      if (mc.currentScreen != null) {
//        Screen screen = mc.currentScreen;
//        if (!(screen instanceof ChatScreen)) {
//          closeInterface();
//        }
//      }
      if (!face.render(event.getMatrixStack(), event.getPartialTicks())) {
        clearInterface();
      }
    }
  }

  @SubscribeEvent
  public static void onHudRender(RenderGameOverlayEvent.Post event) {
    if (getInterface() == null) return;
    if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
      getInterface().renderHud(event.getMatrixStack(), event.getPartialTicks(), event.getWindow());
    }
  }

  @SubscribeEvent
  public static void tick(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      if (getInterface() != null) {
        getInterface().tick();
      }
    }
  }

  @SubscribeEvent
  public static void onMouseInput(InputEvent.RawMouseEvent event) {
    if (getInterface() != null) {
      if (!getInterface().isOpenClose()) {
        if (mc.currentScreen == null) {
          if (getInterface().onMouseInput(event.getButton(), event.getAction(), event.getMods())) {
            event.setCanceled(true);
          }
        }
      }
    }
  }

  @SubscribeEvent
  public static void onMouseScroll(InputEvent.MouseScrollEvent event) {
    if (getInterface() != null) {
      if (!getInterface().isOpenClose()) {
        if (getInterface().onMouseScroll(event.getScrollDelta(), event.isLeftDown(), event.isMiddleDown(), event.isRightDown())) {
          event.setCanceled(true);
        }
      }
    }
  }

  private static boolean shouldCancelKeyEvent(int key, int scanCode) {
    if (getInterface() == null)
      return false;
    return getInterface().shouldCancelKeyEvent(key, scanCode);
  }

  public static void preKeyEvent(int key, int scanCode, int action, int modifiers) {
    if (getInterface() != null) {
      getInterface().preKeyEvent(key, scanCode, action, modifiers);
    }
  }

  @SubscribeEvent
  public static void onKeyEvent(InputEvent.KeyInputEvent event) {
//    if (event.getKey() == GLFW.GLFW_KEY_N && event.getAction() == GLFW.GLFW_PRESS) {
//      try {
//        WormholeRenderer.loadShader();
//        WormholeRenderer.setShaderEnabled(!KeyboardHelper.hasShiftDown());
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
    if (mc.currentScreen != null) return;
    if (getInterface() != null) {
      if (!getInterface().isOpenClose()) {
        getInterface().onKeyEvent(event.getKey(), event.getScanCode(), event.getAction(), event.getModifiers());
      }
    }
    if (KEY_BINDING.isPressed()) {
      if (getInterface() == null) {
        tryOpen(mc.player);
      } else {
        closeInterface();
      }
    }
  }

  public static void charCallback(int codePoint, int modifiers) {
    if (mc.currentScreen == null) {
      if (getInterface() != null) {
        getInterface().onCharEvent(codePoint, modifiers);
      }
    }
  }
}
