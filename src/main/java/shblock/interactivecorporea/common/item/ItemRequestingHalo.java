package shblock.interactivecorporea.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.block.BlockItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NBTTagHelper;
import vazkii.botania.common.block.corporea.BlockCorporeaIndex;
import vazkii.botania.common.core.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemRequestingHalo extends Item {
  private static final String PREFIX_INDEX_POS = "bound_position";
  private static final String PREFIX_SENDER_POS = "sender_position";

  public ItemRequestingHalo() {
    super(new Properties().group(IC.ITEM_GROUP).maxStackSize(1));
  }

  @Override
  public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
    super.inventoryTick(stack, world, entity, slot, isSelected);
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
    ItemStack stack = player.getHeldItem(hand);
    if (world.isRemote) {
      if (hand == Hand.MAIN_HAND) {
        RequestingHaloInterfaceHandler.openInterface(new RequestingHaloInterface(new CISlotPointer(player.inventory.currentItem)));
      }
    }
    return ActionResult.resultSuccess(stack);
  }

  @Nullable
  public static GlobalPos getBoundIndexPosition(ItemStack stack) {
    return NBTTagHelper.getGlobalPos(ItemNBTHelper.get(stack, PREFIX_INDEX_POS));
  }

  @Nullable
  public static GlobalPos getBoundSenderPosition(ItemStack stack) {
    return NBTTagHelper.getGlobalPos(ItemNBTHelper.get(stack, PREFIX_SENDER_POS));
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext context) {
    World world = context.getWorld();
    BlockPos pos = context.getPos();
    Block block = world.getBlockState(pos).getBlock();
    if (context.getPlayer() == null)
      return ActionResultType.PASS;
    if (!world.isRemote) {
      if (context.getPlayer().isSneaking()) {
        String prefix;
        if (block instanceof BlockCorporeaIndex) {
          prefix = PREFIX_INDEX_POS;
        } else if (block instanceof BlockItemQuantizationDevice) {
          prefix = PREFIX_SENDER_POS;
        } else {
          return ActionResultType.CONSUME;
        }
        RegistryKey<World> worldKey = world.getDimensionKey();
        GlobalPos globalPos = GlobalPos.getPosition(worldKey, pos);
        ItemNBTHelper.set(context.getItem(), prefix, NBTTagHelper.putGlobalPos(globalPos));
        return ActionResultType.SUCCESS;
      }
    } else {
      if (context.getPlayer().isSneaking()) {
        if (block instanceof BlockCorporeaIndex || block instanceof BlockItemQuantizationDevice) {
          return ActionResultType.SUCCESS;
        } else {
          return ActionResultType.CONSUME;
        }
      }
    }
    return ActionResultType.PASS;
  }

  @Override
  public boolean canPlayerBreakBlockWhileHolding(BlockState bs, World world, BlockPos pos, PlayerEntity player) {
    return false;
  }
}
