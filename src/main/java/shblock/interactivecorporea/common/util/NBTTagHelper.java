package shblock.interactivecorporea.common.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;

public class NBTTagHelper {
  @Nullable
  public static GlobalPos getGlobalPos(INBT nbt) {
    if (nbt == null)
      return null;
    Pair<GlobalPos, INBT> result = GlobalPos.CODEC.decode(NBTDynamicOps.INSTANCE, nbt).result().orElse(null);
    if (result == null)
      return null;
    return result.getFirst();
  }

  @Nullable
  public static INBT putGlobalPos(GlobalPos pos) {
    if (pos == null)
      return null;
    return GlobalPos.CODEC.encodeStart(NBTDynamicOps.INSTANCE, pos).get().orThrow();
  }

  public static Vector3f getVector3f(CompoundNBT nbt) {
    return new Vector3f(
        nbt.getFloat("x"),
        nbt.getFloat("y"),
        nbt.getFloat("z")
    );
  }

  public static CompoundNBT putVector3f(Vector3f vec) {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putFloat("x", vec.getX());
    nbt.putFloat("y", vec.getY());
    nbt.putFloat("z", vec.getZ());
    return nbt;
  }

  public static Vector3d getVector3d(CompoundNBT nbt) {
    return new Vector3d(
        nbt.getDouble("x"),
        nbt.getDouble("y"),
        nbt.getDouble("z")
    );
  }

  public static CompoundNBT putVector3d(Vector3d vec) {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putDouble("x", vec.getX());
    nbt.putDouble("y", vec.getY());
    nbt.putDouble("z", vec.getZ());
    return nbt;
  }

  public static Vector3 getVector3(CompoundNBT nbt) {
    return new Vector3(
        nbt.getDouble("x"),
        nbt.getDouble("y"),
        nbt.getDouble("z")
    );
  }

  public static CompoundNBT putVector3(Vector3 vec) {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putDouble("x", vec.x);
    nbt.putDouble("y", vec.y);
    nbt.putDouble("z", vec.z);
    return nbt;
  }

  public static World getWorld(CompoundNBT nbt, String tag) {
    Pair<RegistryKey<World>, INBT> result = World.CODEC.decode(NBTDynamicOps.INSTANCE, nbt.get(tag)).result().orElse(null);
    if (result == null) return null;
    RegistryKey<World> reg = result.getFirst();
    return WorldHelper.getWorldFromName(reg);
  }

  public static void putWorld(CompoundNBT nbt, String tag, World world) {
    nbt.put(tag, World.CODEC.encodeStart(NBTDynamicOps.INSTANCE, world.getDimensionKey()).get().orThrow());
  }

  public static CompoundNBT putCurioSlot(CurioSlotPointer slot) {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putString("identifier", slot.identifier);
    nbt.putInt("slot", slot.slot);
    return nbt;
  }

  public static CurioSlotPointer getCurioSlot(CompoundNBT nbt) {
    return new CurioSlotPointer(nbt.getString("identifier"), nbt.getInt("slot"));
  }

  public static CompoundNBT putCISlot(CISlotPointer slot) {
    CompoundNBT nbt = new CompoundNBT();
    nbt.putBoolean("is_inv", slot.isInventory());
    if (slot.isInventory()) {
      nbt.putInt("slot", slot.getInventory());
    } else {
      nbt.put("slot", putCurioSlot(slot.getCurio()));
    }
    return nbt;
  }

  public static CISlotPointer getCISlot(CompoundNBT nbt) {
    if (nbt.getBoolean("is_inv")) {
      return new CISlotPointer(nbt.getInt("slot"));
    } else {
      return new CISlotPointer(getCurioSlot(nbt.getCompound("slot")));
    }
  }
}
