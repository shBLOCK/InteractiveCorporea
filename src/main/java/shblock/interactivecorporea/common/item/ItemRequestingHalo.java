package shblock.interactivecorporea.common.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
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
import shblock.interactivecorporea.common.util.CISlotPointer;
import vazkii.botania.common.core.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemRequestingHalo extends Item {
  private static final String PREFIX_POS = "bound_position";

  public ItemRequestingHalo() {
    super(new Properties().group(IC.ITEM_GROUP));
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
  public static GlobalPos getBoundPosition(ItemStack stack) {
    INBT nbt = ItemNBTHelper.get(stack, PREFIX_POS);
    if (nbt == null) {
      return null;
    }
    Pair<GlobalPos, INBT> result = GlobalPos.CODEC.decode(NBTDynamicOps.INSTANCE, nbt).result().orElse(null);
    if (result == null) {
      return null;
    }
    return result.getFirst();
  }

  @Override
  public ActionResultType onItemUse(ItemUseContext context) {
    if (!context.getWorld().isRemote) {
      if (context.getPlayer() == null) {
        return ActionResultType.PASS;
      }
      if (context.getPlayer().isSneaking()) {
        RegistryKey<World> worldKey = context.getWorld().getDimensionKey();
        INBT nbt = GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, GlobalPos.getPosition(worldKey, context.getPos())).get().orThrow();
        ItemNBTHelper.set(context.getItem(), PREFIX_POS, nbt);
        return ActionResultType.SUCCESS;
      }
    } else {
      if (context.getPlayer() == null) {
        return ActionResultType.PASS;
      }
      if (context.getPlayer().isSneaking()) {
        return ActionResultType.SUCCESS;
      }
    }
    return ActionResultType.PASS;
  }

  @Override
  public boolean canPlayerBreakBlockWhileHolding(BlockState bs, World world, BlockPos pos, PlayerEntity player) {
    return false;
  }
}
