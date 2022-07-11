package shblock.interactivecorporea.common.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.DistExecutor;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.renderer.item.ISTERRequestingHalo;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.util.KeyboardHelper;
import shblock.interactivecorporea.common.block.BlockItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NBTTagHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.common.block.corporea.BlockCorporeaIndex;
import vazkii.botania.common.core.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemRequestingHalo extends Item implements IManaUsingItem {
  private static final String PREFIX_INDEX_POS = "bound_position";
  private static final String PREFIX_SENDER_POS = "sender_position";
  private static final String PREFIX_MODULES = "modules";
  private static final String PREFIX_CRAFTING_SLOT_ITEMS = "crafting_slot_items";

  public ItemRequestingHalo() {
    super(new Properties().group(IC.ITEM_GROUP).maxStackSize(1).setISTER(() -> ISTERRequestingHalo::new));
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
        if (RequestingHaloInterfaceHandler.isInterfaceOpened()) {
          RequestingHaloInterfaceHandler.closeInterface();
        } else {
          RequestingHaloInterfaceHandler.openInterface(new RequestingHaloInterface(new CISlotPointer(player.inventory.currentItem)));
        }
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

  /**
   * Try to install a module
   * @return if the installation was successful
   */
  public static boolean installModule(ItemStack stack, HaloModule module) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    if (module.containsThis(mask)) return false;
    ItemNBTHelper.setInt(stack, PREFIX_MODULES, mask | module.bitMask);
    return true;
  }

  public static boolean uninstallModule(ItemStack stack, HaloModule module) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    if (module.containsThis(mask)) {
      ItemNBTHelper.setInt(stack, PREFIX_MODULES, mask - module.bitMask);
      return true;
    }
    return false;
  }

  public static boolean isModuleInstalled(ItemStack stack, HaloModule module) {
    int mask = ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0);
    return module.containsThis(mask);
  }

  public static boolean isAnyModuleInstalled(ItemStack stack) {
    return ItemNBTHelper.getInt(stack, PREFIX_MODULES, 0) != 0;
  }

  public static ListNBT getOrCreateCraftingSlotNBTList(ItemStack halo) {
    boolean didChange = false;
    ListNBT nbt = ItemNBTHelper.getList(halo, PREFIX_CRAFTING_SLOT_ITEMS, Constants.NBT.TAG_COMPOUND, true);
    if (nbt == null) {
      nbt = new ListNBT();
      didChange = true;
    }
    for (int i = nbt.size(); i < 9; i++) {
      nbt.add(new CompoundNBT());
    }
    if (didChange)
      ItemNBTHelper.setList(halo, PREFIX_CRAFTING_SLOT_ITEMS, nbt);
    return nbt;
  }

  /**
   * Should only be called on server!!!
   * @param replaceHandler called when the stack in that slot got replaced (probably drop the item as entity right here)
   * @param addStackHandler called when the adding stack got changed (with the changed stack passed in)
   * @return the old item in that slot
   */
  public static boolean tryPutStackInCraftingSlot(ItemStack halo, int slot, ItemStack orgAddStack, Consumer<ItemStack> replaceHandler, Consumer<ItemStack> addStackHandler) {
    if (slot > 8 || slot < 0 || orgAddStack.isEmpty()) return false;

    ItemStack addStack = orgAddStack.copy();
    ItemStack orgStack = getStackInCraftingSlot(halo, slot);

    Pair<Boolean, ItemStack> addResult = StackHelper.addToAnotherStack(addStack, orgStack);
    orgStack = addResult.getSecond();

    ListNBT list = getOrCreateCraftingSlotNBTList(halo);
    if (addResult.getFirst()) {
      addStackHandler.accept(addStack);
      list.set(slot, orgStack.write(new CompoundNBT()));
    } else {
      replaceHandler.accept(orgStack);
      addStackHandler.accept(ItemStack.EMPTY);
      list.set(slot, addStack.write(new CompoundNBT()));
    }

    return true;
  }

  /**
   * This could be called on both client and server
   */
  public static ItemStack getStackInCraftingSlot(ItemStack halo, int slot) {
    ListNBT list = getOrCreateCraftingSlotNBTList(halo);
    return ItemStack.read(list.getCompound(slot));
  }

  public static ItemStack setStackInCraftingSlot(ItemStack halo, int slot, ItemStack newStack) {
    ListNBT list = getOrCreateCraftingSlotNBTList(halo);
    ItemStack oldStack = ItemStack.read(list.getCompound(slot));
    list.set(slot, newStack.isEmpty() ? new CompoundNBT() : newStack.write(new CompoundNBT()));
    return oldStack;
  }

  private String globalPosToString(GlobalPos pos) {
    return pos.getDimension().getLocation() + " (" + pos.getPos().getCoordinatesAsString() + ")";
  }

  @Override
  public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    GlobalPos indexPos = getBoundIndexPosition(stack);
    GlobalPos senderPos = getBoundSenderPosition(stack);

    if (KeyboardHelper.hasShiftDown()) {
      String temp;
      temp = indexPos == null ? I18n.format(IC.MODID + ".requesting_halo.tooltip.null") : "\n    " + globalPosToString(indexPos);
      tooltip.add(new StringTextComponent(I18n.format(IC.MODID + ".requesting_halo.tooltip.index_pos") + temp));

      temp = senderPos == null ? I18n.format(IC.MODID + ".requesting_halo.tooltip.null") : "\n    " + globalPosToString(senderPos);
      tooltip.add(new StringTextComponent(I18n.format(IC.MODID + ".requesting_halo.tooltip.sender_pos") + temp));

      if (ItemRequestingHalo.isAnyModuleInstalled(stack)) {
        StringBuilder builder = new StringBuilder(I18n.format(IC.MODID + ".requesting_halo.tooltip.modules_prefix"));
        builder.append("§r| ");
        for (HaloModule module : HaloModule.values()) {
          boolean installed = isModuleInstalled(stack, module);
          builder.append(installed ? "§6" : "§8");
          builder.append(I18n.format(module.translationKey));
          builder.append("§r | ");
        }
        tooltip.add(new StringTextComponent(builder.toString()));
      } else {
        tooltip.add(new StringTextComponent(
            I18n.format(IC.MODID + ".requesting_halo.tooltip.modules_prefix") +
                I18n.format(IC.MODID + ".requesting_halo.tooltip.null")
        ));
      }
    } else {
      tooltip.add(new TranslationTextComponent(IC.MODID + ".tooltip.shift_for_more"));
    }
  }

  @Override
  public boolean canPlayerBreakBlockWhileHolding(BlockState bs, World world, BlockPos pos, PlayerEntity player) {
    return false;
  }

  @Override
  public boolean usesMana(ItemStack stack) {
    return true;
  }

  @Override
  public boolean shouldSyncTag() {
    return super.shouldSyncTag();
  }
}
