package shblock.interactivecorporea.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

public class NetworkHelper {
  public static void writeCurioSlotPointer(PacketBuffer buffer, CurioSlotPointer slot) {
    buffer.writeString(slot.identifier);
    buffer.writeInt(slot.slot);
  }

  public static CurioSlotPointer readCurioSlotPointer(PacketBuffer buffer) {
    return new CurioSlotPointer(buffer.readString(), buffer.readInt());
  }

  public static void writeCISlotPointer(PacketBuffer buffer, CISlotPointer slot) {
    if (slot.isInventory()) {
      buffer.writeBoolean(true);
      buffer.writeInt(slot.getInventory());
    } else {
      buffer.writeBoolean(false);
      writeCurioSlotPointer(buffer, slot.getCurio());
    }
  }

  public static CISlotPointer readCISlotPointer(PacketBuffer buffer) {
    if (buffer.readBoolean()) { // inventory
      return new CISlotPointer(buffer.readInt());
    } else { // curio
      return new CISlotPointer(readCurioSlotPointer(buffer));
    }
  }

  public static void writeBigStack(PacketBuffer buffer, ItemStack stack, boolean limitedTag) {
    if (stack.isEmpty()) {
      buffer.writeBoolean(false);
    } else {
      buffer.writeBoolean(true);
      Item item = stack.getItem();
      buffer.writeVarInt(Item.getIdFromItem(item));
      buffer.writeVarInt(stack.getCount());
      CompoundNBT compoundnbt = null;
      if (item.isDamageable(stack) || item.shouldSyncTag()) {
        compoundnbt = limitedTag ? stack.getShareTag() : stack.getTag();
      }

      buffer.writeCompoundTag(compoundnbt);
    }
  }

  public static ItemStack readBigStack(PacketBuffer buffer) {
    if (!buffer.readBoolean()) {
      return ItemStack.EMPTY;
    } else {
      int i = buffer.readVarInt();
      int j = buffer.readVarInt();
      ItemStack itemstack = new ItemStack(Item.getItemById(i), j);
      itemstack.readShareTag(buffer.readCompoundTag());
      return itemstack;
    }
  }
}
