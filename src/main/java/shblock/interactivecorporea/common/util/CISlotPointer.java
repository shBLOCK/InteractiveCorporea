package shblock.interactivecorporea.common.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/**
 * Represents either an inventory slot or a curio slot
 */
public class CISlotPointer {
  private int slot = -1;
  private CurioSlotPointer cSlot = null;

  public CISlotPointer(int slot) {
    this.slot = slot;
  }

  public CISlotPointer(CurioSlotPointer cSlot) {
    this.cSlot = cSlot;
  }

  public boolean isInventory() {
    return slot != -1;
  }

  public boolean isCurio() {
    return cSlot != null;
  }

  public int getInventory() {
    return slot;
  }

  public CurioSlotPointer getCurio() {
    return cSlot;
  }

  public ItemStack getStack(PlayerEntity player) {
    if (slot != -1) {
      return player.inventory.getStackInSlot(slot);
    } else {
      return CuriosHelper.getPointedSlot(player, cSlot);
    }
  }
}
