package shblock.interactivecorporea.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;

import javax.annotation.Nullable;

public class BlockItemQuantizationDevice extends Block {
  public static final String NAME = "item_quantization_device";

  public BlockItemQuantizationDevice() {
    super(Properties.create(Material.IRON).hardnessAndResistance(5.5F).sound(SoundType.METAL).notSolid());
  }

  @Override
  public boolean hasComparatorInputOverride(BlockState state) {
    return true;
  }

  @Override
  public int getComparatorInputOverride(BlockState blockState, World world, BlockPos pos) {
    TileEntity te = world.getTileEntity(pos);
    if (te instanceof TileItemQuantizationDevice) {
      TileItemQuantizationDevice iqd = (TileItemQuantizationDevice) te;
      return iqd.getComparatorLevel();
    }
    return 0;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new TileItemQuantizationDevice();
  }
}
