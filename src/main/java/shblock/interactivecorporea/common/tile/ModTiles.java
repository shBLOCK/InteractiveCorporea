package shblock.interactivecorporea.common.tile;

import net.minecraft.tileentity.TileEntityType;
import shblock.interactivecorporea.common.block.ModBlocks;

@SuppressWarnings("ConstantConditions")
public class ModTiles {
  public static final TileEntityType<TileItemQuantizationDevice> itemQuantizationDevice = TileEntityType.Builder.create(TileItemQuantizationDevice::new, ModBlocks.itemQuantizationDevice).build(null);
}
