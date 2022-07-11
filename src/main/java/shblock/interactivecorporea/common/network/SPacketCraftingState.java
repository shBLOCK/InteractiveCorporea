package shblock.interactivecorporea.common.network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SPacketCraftingState {
  public static final byte FAILED = 0;
  public static final byte STARTED = 1;
  public static final byte SUCCEED = 2;

  private final int type;

  public SPacketCraftingState(byte type) {
    this.type = type;
  }

  public static SPacketCraftingState decode(PacketBuffer buf) {
    return new SPacketCraftingState(buf.readByte());
  }

  public void encode(PacketBuffer buf) {
    buf.writeByte(type);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {

    });
    ctx.get().setPacketHandled(true);
  }
}
