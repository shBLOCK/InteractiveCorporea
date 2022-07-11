package shblock.interactivecorporea.common.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.util.NetworkHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SPacketUpdateItemList {
  private final List<ItemStack> itemList;

  public SPacketUpdateItemList(List<ItemStack> itemList) {
    this.itemList = itemList;
  }

  public static SPacketUpdateItemList decode(PacketBuffer buf) {
    int len = buf.readVarInt();
    List<ItemStack> itemList = new ArrayList<>();
    for (int i = 0; i < len; i++) {
      itemList.add(NetworkHelper.readBigStack(buf));
    }
    return new SPacketUpdateItemList(itemList);
  }

  public void encode(PacketBuffer buf) {
    buf.writeVarInt(itemList.size());
    for (ItemStack stack : itemList) {
      NetworkHelper.writeBigStack(buf, stack, false);
    }
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      IC.debug("Update packet received");
      RequestingHaloInterfaceHandler.handleUpdatePacket(itemList);
    });
    ctx.get().setPacketHandled(true);
  }
}
