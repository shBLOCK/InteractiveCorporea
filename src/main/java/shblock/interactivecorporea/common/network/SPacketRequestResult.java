package shblock.interactivecorporea.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

import java.util.function.Supplier;

public class SPacketRequestResult {
  private final int requestId;
  private final int successAmount;

  public SPacketRequestResult(int requestId, int successAmount) {
    this.requestId = requestId;
    this.successAmount = successAmount;
  }

  public static SPacketRequestResult decode(PacketBuffer buf) {
    return new SPacketRequestResult(buf.readInt(), buf.readInt());
  }

  public void encode(PacketBuffer buf) {
    buf.writeInt(requestId);
    buf.writeInt(successAmount);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      RequestingHaloInterfaceHandler.handleRequestResultPacket(requestId, successAmount);
    });
    ctx.get().setPacketHandled(true);
  }
}
