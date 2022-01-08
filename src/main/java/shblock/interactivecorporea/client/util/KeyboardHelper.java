package shblock.interactivecorporea.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;

public class KeyboardHelper {
  private static final long window = Minecraft.getInstance().getMainWindow().getHandle();

  public static boolean hasControlDown() {
    if (Minecraft.IS_RUNNING_ON_MAC) {
      return InputMappings.isKeyDown(window, 343) || InputMappings.isKeyDown(window, 347);
    } else {
      return InputMappings.isKeyDown(window, 341) || InputMappings.isKeyDown(window, 345);
    }
  }

  public static boolean hasShiftDown() {
    return InputMappings.isKeyDown(window, 340) || InputMappings.isKeyDown(window, 344);
  }

  public static boolean hasAltDown() {
    return InputMappings.isKeyDown(window, 342) || InputMappings.isKeyDown(window, 346);
  }
}
