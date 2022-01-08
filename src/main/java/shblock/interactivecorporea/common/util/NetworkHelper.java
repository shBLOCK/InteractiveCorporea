package shblock.interactivecorporea.common.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import vazkii.botania.common.core.helper.Vector3;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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

  public static void writeGlobalPos(PacketBuffer buffer, GlobalPos pos) {
//    buffer.writeResourceLocation(pos.getDimension().getRegistryName());
//    buffer.writeResourceLocation(pos.getDimension().getLocation());
//    buffer.writeBlockPos(pos.getPos());
    CompoundNBT nbt = (CompoundNBT) NBTTagHelper.putGlobalPos(pos);
    buffer.writeCompoundTag(nbt == null ? new CompoundNBT() : nbt);
  }

  public static GlobalPos readGlobalPos(PacketBuffer buffer) {
//    try {
//      Constructor<RegistryKey> constructor = RegistryKey.class.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
//      RegistryKey<World> registryKey = constructor.newInstance(buffer.readResourceLocation(), buffer.readResourceLocation());
//      return GlobalPos.getPosition(registryKey, buffer.readBlockPos());
//    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
//      e.printStackTrace();
//      return null;
//    }
    CompoundNBT nbt = buffer.readCompoundTag();
    return NBTTagHelper.getGlobalPos(nbt);
  }

  public static void writeVector3f(PacketBuffer buffer, Vector3f vec) {
    buffer.writeFloat(vec.getX());
    buffer.writeFloat(vec.getY());
    buffer.writeFloat(vec.getZ());
  }

  public static Vector3f readVector3f(PacketBuffer buffer) {
    return new Vector3f(
        buffer.readFloat(),
        buffer.readFloat(),
        buffer.readFloat()
    );
  }

  public static void writeVector3d(PacketBuffer buffer, Vector3d vec) {
    buffer.writeDouble(vec.getX());
    buffer.writeDouble(vec.getY());
    buffer.writeDouble(vec.getZ());
  }

  public static Vector3d readVector3d(PacketBuffer buffer) {
    return new Vector3d(
        buffer.readDouble(),
        buffer.readDouble(),
        buffer.readDouble()
    );
  }

  public static void writeVector3(PacketBuffer buffer, Vector3 vec) {
    buffer.writeDouble(vec.x);
    buffer.writeDouble(vec.y);
    buffer.writeDouble(vec.z);
  }

  public static Vector3 readVector3(PacketBuffer buffer) {
    return new Vector3(
        buffer.readDouble(),
        buffer.readDouble(),
        buffer.readDouble()
    );
  }
}
