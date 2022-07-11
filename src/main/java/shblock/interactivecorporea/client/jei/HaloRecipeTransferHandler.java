package shblock.interactivecorporea.client.jei;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.ingredient.IGuiIngredient;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

import javax.annotation.Nullable;
import java.util.Map;

public class HaloRecipeTransferHandler implements IRecipeTransferHandler<DummyTransferringContainer> {
  @Override
  public Class<DummyTransferringContainer> getContainerClass() {
    return DummyTransferringContainer.class;
  }

  private static boolean shouldClose() {
    return RequestingHaloInterfaceHandler.getInterface() == null || RequestingHaloInterfaceHandler.getInterface().isOpenClose();
  }

  @Nullable
  @Override
  public IRecipeTransferError transferRecipe(DummyTransferringContainer container, Object recipe, IRecipeLayout recipeLayout, PlayerEntity player, boolean maxTransfer, boolean doTransfer) {
    if (shouldClose()) {
      Minecraft.getInstance().displayGuiScreen(null);
      return null;
    }
    if (doTransfer) {
      boolean didChangeRecipe = false;
      for (Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : recipeLayout.getItemStacks().getGuiIngredients().entrySet()) {
        int index = entry.getKey();
        IGuiIngredient<ItemStack> ingredient = entry.getValue();
        if (ingredient == null) continue;
        if (!ingredient.isInput()) continue;
        ItemStack stack = ingredient.getDisplayedIngredient();
        if (RequestingHaloInterfaceHandler.getInterface().getCraftingInterface().tryPlaceShadowItem(index - 1, stack == null ? ItemStack.EMPTY : stack))
          didChangeRecipe = true;
      }

      if (didChangeRecipe)
        RequestingHaloInterfaceHandler.getInterface().getCraftingInterface().updateRecipe();

      container.shouldClose = true;
    } else {
      //TODO: display red overlay?
    }
    return null;
  }
}
