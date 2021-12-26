package shblock.interactivecorporea.common.corporea;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import vazkii.botania.api.corporea.ICorporeaRequestMatcher;

public class CorporeaAllMatcher implements ICorporeaRequestMatcher {
  @Override
  public boolean test(ItemStack stack) {
    return true;
  }

  @Override
  public ITextComponent getRequestName() {
    return new StringTextComponent("internal request");
  }
}
