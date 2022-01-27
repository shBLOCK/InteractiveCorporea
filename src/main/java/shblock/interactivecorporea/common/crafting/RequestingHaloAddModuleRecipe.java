package shblock.interactivecorporea.common.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;

public class RequestingHaloAddModuleRecipe extends SpecialRecipe {
  public static final SpecialRecipeSerializer<RequestingHaloAddModuleRecipe> SERIALIZER = new SpecialRecipeSerializer<>(RequestingHaloAddModuleRecipe::new);

  public RequestingHaloAddModuleRecipe(ResourceLocation id) {
    super(id);
  }

  @Override
  public boolean matches(CraftingInventory inv, World world) {
    boolean foundHalo = false;
    boolean foundModule = false;

    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (foundHalo) return false;
          foundHalo = true;
        } else {
          if (HaloModule.fromItem(stack.getItem().getRegistryName()) != null) {
            if (foundModule) return false;
            foundModule = true;
          } else {
            return false;
          }
        }
      }
    }

    return foundHalo && foundModule;
  }

  @Override
  public ItemStack getCraftingResult(CraftingInventory inv) {
    ItemStack halo = null;
    HaloModule module = null;

    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (halo != null) return ItemStack.EMPTY;
          halo = stack.copy();
        } else {
          HaloModule m = HaloModule.fromItem(stack.getItem().getRegistryName());
          if (m != null) {
            if (module != null) return ItemStack.EMPTY;
            module = m;
          } else {
            return ItemStack.EMPTY;
          }
        }
      }
    }

    if (halo == null || module == null) return ItemStack.EMPTY;

    if (!ItemRequestingHalo.installModule(halo, module)) {
      return ItemStack.EMPTY;
    }

    return halo;
  }

  @Override
  public boolean canFit(int width, int height) {
    return width * height >= 2;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return SERIALIZER;
  }
}
