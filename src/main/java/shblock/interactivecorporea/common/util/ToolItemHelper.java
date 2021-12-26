package shblock.interactivecorporea.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.function.Predicate;

public class ToolItemHelper {
  /**
   * Gets the first inventory slot of a player, of which the ItemStack in that slot matches the matcher
   */
  public static int getFirstMatchedSlotInInventory(PlayerEntity player, Predicate<ItemStack> matcher) {
    for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) {
      ItemStack stack = player.inventory.getStackInSlot(slot);
      if (matcher.test(stack)) {
        return slot;
      }
    }
    return -1;
  }

  public static int getFirstMatchedSlotInInventory(PlayerEntity player, Class<? extends Item> matcher) {
    return getFirstMatchedSlotInInventory(player, stack -> stack.getItem().getClass() == matcher);
  }

  /**
   * Gets the first curio slot of a player, of which the ItemStack in that slot matches the matcher
   */
  public static CurioSlotPointer getFirstMatchedCurioSlot(LivingEntity entity, Predicate<ItemStack> matcher) {
    ICuriosItemHandler curiosHandler = CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
    if (curiosHandler == null) return null;
    Map<String, ICurioStacksHandler> curios = curiosHandler.getCurios();
    for (String cName : curios.keySet()) {
      IDynamicStackHandler stackHandler = curios.get(cName).getStacks();
      for (int i = 0; i < stackHandler.getSlots(); i++) {
        ItemStack stack = stackHandler.getStackInSlot(i);
        if (matcher.test(stack)) {
          return new CurioSlotPointer(cName, i);
        }
      }
    }
    return null;
  }

  public static CurioSlotPointer getFirstMatchedCurioSlot(LivingEntity entity, Class<? extends Item> matcher) {
    return getFirstMatchedCurioSlot(entity, stack -> stack.getItem().getClass() == matcher);
  }
}
