package shblock.interactivecorporea.common.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;

public class CuriosHelper {
  public static ItemStack getPointedSlot(LivingEntity entity, CurioSlotPointer pointer) {
    ICuriosItemHandler handler = CuriosApi.getCuriosHelper().getCuriosHandler(entity).orElse(null);
    if (handler == null) return null;
    Map<String, ICurioStacksHandler> curios = handler.getCurios();
    if (curios != null) {
      ICurioStacksHandler stacksHandler = curios.get(pointer.identifier);
      if (stacksHandler != null) {
        IDynamicStackHandler stacks = stacksHandler.getStacks();
        if (pointer.slot < stacks.getSlots()) {
          return stacks.getStackInSlot(pointer.slot);
        }
      }
    }
    return null;
  }
}
