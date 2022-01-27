package shblock.interactivecorporea.common.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import vazkii.botania.common.crafting.recipe.RecipeUtils;

public class RequestingHaloRemoveModuleRecipe extends SpecialRecipe {
  public static final SpecialRecipeSerializer<RequestingHaloRemoveModuleRecipe> SERIALIZER = new SpecialRecipeSerializer<>(RequestingHaloRemoveModuleRecipe::new);

  public RequestingHaloRemoveModuleRecipe(ResourceLocation id) {
    super(id);
  }

  @Override
  public boolean matches(CraftingInventory inv, World world) {
    boolean foundHalo = false;

    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (!ItemRequestingHalo.isAnyModuleInstalled(stack)) return false;
          if (foundHalo) return false;
          foundHalo = true;
        } else {
          return false;
        }
      }
    }

    return foundHalo;
  }

  @Override
  public ItemStack getCraftingResult(CraftingInventory inv) {
    ItemStack halo = null;

    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (!stack.isEmpty()) {
        if (stack.getItem() instanceof ItemRequestingHalo) {
          if (!ItemRequestingHalo.isAnyModuleInstalled(stack)) return ItemStack.EMPTY;
          if (halo != null) return ItemStack.EMPTY;
          halo = stack;
        } else {
          return ItemStack.EMPTY;
        }
      }
    }

    if (halo == null) return ItemStack.EMPTY;

    for (HaloModule module : HaloModule.values()) {
      if (ItemRequestingHalo.isModuleInstalled(halo, module)) {
        return new ItemStack(module.getItem());
      }
    }

    return ItemStack.EMPTY;
  }

  @Override
  public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
    return RecipeUtils.getRemainingItemsSub(inv,
        s -> {
            if (s.getItem() instanceof ItemRequestingHalo) {
            ItemStack hs = s.copy();
            for (HaloModule module : HaloModule.values()) {
              if (ItemRequestingHalo.uninstallModule(hs, module)) {
                return hs;
              }
            }
          }
          return null;
        });
  }

  @Override
  public boolean canFit(int width, int height) {
    return width + height > 0;
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return SERIALIZER;
  }
}
