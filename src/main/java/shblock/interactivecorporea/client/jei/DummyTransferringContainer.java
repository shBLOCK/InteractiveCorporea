package shblock.interactivecorporea.client.jei;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

public class DummyTransferringContainer extends Container {
  public boolean shouldClose = false;

  protected DummyTransferringContainer() {
    super(null, 0);
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerIn) {
    return true;
  }
}
