package shblock.interactivecorporea.client.requestinghalo;

import net.minecraft.item.ItemStack;

import java.util.Comparator;

public enum SortMode {
  DICT((a, b) -> {
    return a.getDisplayName().getString().compareTo(b.getDisplayName().getString());
  }),
  AMOUNT((a, b) -> {
    if (a.getCount() == b.getCount()) {
      return DICT.getSorter().compare(a, b);
    }
    return Integer.compare(a.getCount(), b.getCount());
  });

  private Comparator<ItemStack> sorter;

  SortMode(Comparator<ItemStack> sorter) {
    this.sorter = sorter;
  }

  public Comparator<ItemStack> getSorter() {
    return sorter;
  }
}
