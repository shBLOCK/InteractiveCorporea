package shblock.interactivecorporea.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;
import shblock.interactivecorporea.common.util.WorldHelper;
import vazkii.botania.common.core.helper.Vector3;

import java.util.function.Supplier;

public class PacketRequestItem {
  private final CISlotPointer slot;
  private final ItemStack stack;
  private final Vector3 requestPos;
  private final Vector3 normal;

  public PacketRequestItem(CISlotPointer slot, ItemStack stack, Vector3 requestPos, Vector3 normal) {
    this.slot = slot;
    this.stack = stack;
    this.requestPos = requestPos;
    this.normal = normal;
  }

  public static PacketRequestItem decode(PacketBuffer buf) {
    return new PacketRequestItem(NetworkHelper.readCISlotPointer(buf), NetworkHelper.readBigStack(buf), NetworkHelper.readVector3(buf), NetworkHelper.readVector3(buf));
  }

  public void encode(PacketBuffer buf) {
    NetworkHelper.writeCISlotPointer(buf, slot);
    NetworkHelper.writeBigStack(buf, stack, false);
    NetworkHelper.writeVector3(buf, requestPos);
    NetworkHelper.writeVector3(buf, normal);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayerEntity player = ctx.get().getSender();
      if (player == null) return;
      ItemStack halo = slot.getStack(player);
      if (!(halo.getItem() instanceof ItemRequestingHalo)) return;
      GlobalPos pos = ItemRequestingHalo.getBoundSenderPosition(halo);
      if (pos == null) return;
      World world = WorldHelper.getWorldFromName(pos.getDimension());
      if (world == null) return;
      TileEntity te = world.getTileEntity(pos.getPos());
      if (!(te instanceof TileItemQuantizationDevice)) return;
      TileItemQuantizationDevice qd = (TileItemQuantizationDevice) te;
      qd.requestItem(stack, requestPos, normal, player);
      //TODO: send action result packet to client for animation
    });
  }
}
