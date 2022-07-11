package shblock.interactivecorporea.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.item.ItemStack;
import vazkii.botania.common.core.helper.ItemNBTHelper;

public class StackHelper {
  public static boolean equalItemAndTag(ItemStack a, ItemStack b) {
    return !b.isEmpty() && !b.isEmpty() && a.isItemEqual(b) && ItemNBTHelper.matchTag(b.getTag(), a.getTag());
  }

  /**
   * Try stack two stacks together
   * @return A: if the stacks got changed, B: the changed org stack (can be a different object if the org stack was empty)
   */
  public static Pair<Boolean, ItemStack> addToAnotherStack(ItemStack add, ItemStack org) {
    if (equalItemAndTag(org, add) || (org.isEmpty() && !add.isEmpty())) {
      int orgCnt = org.getCount();
      int addCnt = add.getCount();
      int stackLimit = add.getMaxStackSize();
      int transferred = Math.min(addCnt, stackLimit - orgCnt);
      if (transferred == 0) return new Pair<>(false, org);
      if (org.isEmpty()) {
        org = new ItemStack(add.getItem(), transferred);
      } else {
        org.grow(transferred);
      }
      add.shrink(transferred);
      return new Pair<>(true, org);
    }
    return new Pair<>(false, org);
  }
}
