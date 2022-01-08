package shblock.interactivecorporea.common.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.IStringSerializable;

public class BlockItemWormholeProjector extends Block {
  private static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

  public BlockItemWormholeProjector() {
    super(AbstractBlock.Properties.create(Material.ROCK).hardnessAndResistance(2, 10).sound(SoundType.STONE));
    setDefaultState(getStateContainer().getBaseState().with(TYPE, Type.BOTTOM));
  }

  @Override
  protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
    builder.add(TYPE);
  }

  public enum Type implements IStringSerializable {
    TOP("top"),
    BOTTOM("bottom");

    private final String name;

    Type(String name) {
      this.name = name;
    }

    public String toString() {
      return this.name;
    }

    public String getString() {
      return this.name;
    }
  }
}
