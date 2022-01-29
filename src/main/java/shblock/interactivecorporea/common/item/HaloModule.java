package shblock.interactivecorporea.common.item;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import shblock.interactivecorporea.IC;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nullable;

public enum HaloModule {
  HUD("hud", ModItems.monocle.getRegistryName()),
  RECEIVE("receive", ModBlocks.lightRelayDefault.getRegistryName()),
  SEARCH("search", ModItems.itemFinder.getRegistryName()),
  UPDATE("update", ModBlocks.hourglass.getRegistryName()),
  AMOUNT_SORT("amount_sort", ModItems.corporeaSparkMaster.getRegistryName()),
  MAGNATE("magnate", ModItems.magnetRing.getRegistryName()),
  CRAFTING("crafting", ModItems.craftingHalo.getRegistryName());

  public final int bitMask;
  public final String translationKey;
  public final ResourceLocation item;

  HaloModule(String name, ResourceLocation item) {
    this.bitMask = 1 << ordinal();
    this.translationKey = IC.MODID + ".halo_module." + name;
    this.item = item;
  }

  public boolean containsThis(int mask) {
    return (mask & bitMask) != 0;
  }

  public Item getItem() {
    return ForgeRegistries.ITEMS.getValue(item);
  }

  @Nullable
  public static HaloModule fromItem(ResourceLocation item) {
    for (HaloModule module : HaloModule.values()) {
      if (module.item.equals(item)) {
        return module;
      }
    }
    return null;
  }
}
