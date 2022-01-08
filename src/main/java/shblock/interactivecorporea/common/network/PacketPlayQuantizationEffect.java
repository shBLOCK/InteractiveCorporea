package shblock.interactivecorporea.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.client.particle.QuantizationParticleData;
import shblock.interactivecorporea.common.util.NetworkHelper;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Supplier;

public class PacketPlayQuantizationEffect {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final Random RAND = new Random();

  public static final int QUANTIZATION = 0;
  public static final int CONSTRUCTION = 1;

  private final int type; // 0: quantization effect (in quantization device), 1: construction effect (on halo)
  private final ItemStack stack;
  private final int time;
  private final Vector3 pos;
  private final Vector3 normal;

  private PacketPlayQuantizationEffect(int type, ItemStack stack, int time, Vector3 pos, @Nullable Vector3 normal) {
    this.type = type;
    this.stack = stack;
    this.time = time;
    this.pos = pos;
    this.normal = normal;
  }

  public PacketPlayQuantizationEffect(ItemStack stack, int time, Vector3 pos) {
    this(0, stack, time, pos, null);
  }

  public PacketPlayQuantizationEffect(ItemStack stack, int time, Vector3 pos, Vector3 normal) {
    this(1, stack, time, pos, normal);
  }

  public static PacketPlayQuantizationEffect decode(PacketBuffer buf) {
    switch (buf.readInt()) {
      case QUANTIZATION:
        return new PacketPlayQuantizationEffect(
            buf.readItemStack(),
            buf.readVarInt(),
            NetworkHelper.readVector3(buf)
        );
      case CONSTRUCTION:
        return new PacketPlayQuantizationEffect(
            buf.readItemStack(),
            buf.readVarInt(),
            NetworkHelper.readVector3(buf),
            NetworkHelper.readVector3(buf)
        );
    }
    assert false;
    return null;
  }

  @SuppressWarnings("ConstantConditions")
  public void encode(PacketBuffer buf) {
    buf.writeInt(type);
    buf.writeItemStack(stack);
    buf.writeVarInt(time);
    NetworkHelper.writeVector3(buf, pos);
    if (type == CONSTRUCTION) {
      NetworkHelper.writeVector3(buf, normal);
    }
  }

  @SuppressWarnings("ConstantConditions")
  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      if (mc.world == null) return;
      QuantizationParticleData data;
      switch (type) {
        case QUANTIZATION:
          for (int i = 0; i < 512; i++) {
            double particleDist = 2;
            Vector3 dest = new Vector3(RAND.nextDouble() * 2 - 1, RAND.nextDouble() * 2 - 1, RAND.nextDouble() * 2 - 1)
                .normalize()
                .multiply(particleDist);
            data = new QuantizationParticleData(dest, time, stack, true);
            mc.world.addParticle(data, pos.x, pos.y, pos.z, 0, 0, 0);
          }
          break;
        case CONSTRUCTION:
          Vector3 rotBase = normal.perpendicular().normalize().multiply(.5);
          for (int i = 0; i < 128; i++) {
            Vector3 roted = rotBase.rotate(RAND.nextDouble() * Math.PI * 2, normal);
            Vector3 p = roted.add(pos);
            data = new QuantizationParticleData(roted.negate(), time, stack, false);
            mc.world.addParticle(data, p.x, p.y, p.z, 0, 0, 0);
          }
          break;
        default:
          assert false;
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
