package shblock.interactivecorporea.common.util;

import net.minecraft.item.ItemStack;

import java.util.List;

public class ItemListHelper {
  public static void addToListCompacted(List<ItemStack> list, ItemStack stack) {
    for (ItemStack s : list) {
      if (StackHelper.equalItemAndTag(s, stack)) {
        s.grow(stack.getCount());
        return;
      }
    }
    list.add(stack);
  }
}
