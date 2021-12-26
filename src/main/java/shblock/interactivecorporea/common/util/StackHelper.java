package shblock.interactivecorporea.common.util;

import net.minecraft.item.ItemStack;
import vazkii.botania.common.core.helper.ItemNBTHelper;

public class StackHelper {
  public static boolean equalItemAndTag(ItemStack a, ItemStack b) {
    return !b.isEmpty() && !b.isEmpty() && a.isItemEqual(b) && ItemNBTHelper.matchTag(b.getTag(), a.getTag());
  }
}
