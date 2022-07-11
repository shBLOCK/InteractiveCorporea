package shblock.interactivecorporea.client.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

@JeiPlugin
public class ICJeiPlugin implements IModPlugin {
  private static final ResourceLocation ID = new ResourceLocation(IC.MODID, "main");

  @Override
  public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
    registration.addRecipeTransferHandler(new HaloRecipeTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    // [Botania Copy]
    RequestingHaloInterfaceHandler.jeiUnderMouseGetter = () -> {
      Object o = jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();

      if (o == null && Minecraft.getInstance().currentScreen == jeiRuntime.getRecipesGui()) {
        o = jeiRuntime.getRecipesGui().getIngredientUnderMouse();
      }

      if (o == null) {
        o = jeiRuntime.getBookmarkOverlay().getIngredientUnderMouse();
      }

      if (o instanceof ItemStack) {
        return (ItemStack) o;
      }
      return null;
    };
  }

  @Override
  public ResourceLocation getPluginUid() {
    return ID;
  }
}
