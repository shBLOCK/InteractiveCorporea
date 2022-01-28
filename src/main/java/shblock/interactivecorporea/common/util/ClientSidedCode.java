package shblock.interactivecorporea.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.ModSounds;
import shblock.interactivecorporea.client.particle.QuantizationParticleData;
import shblock.interactivecorporea.common.network.PacketPlayQuantizationEffect;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ClientSidedCode {
  @Nullable
  public static World getWorldFromName(RegistryKey<World> key) {
    if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
      return ServerSidedCode.getWorldFromName(key);
    } else {
      World world = Minecraft.getInstance().world;
      if (world.getDimensionKey().equals(key)) {
        return world;
      } else {
        return null;
      }
    }
  }

  public static void handlePacketPlayQuantizationEffect(int type, ItemStack stack, int time, Vector3 pos, Vector3 normal, Supplier<NetworkEvent.Context> ctx) {
    Minecraft mc = Minecraft.getInstance();
    ctx.get().enqueueWork(() -> {
      if (mc.world == null) return;
      QuantizationParticleData data;
      switch (type) {
        case PacketPlayQuantizationEffect.QUANTIZATION:
          mc.world.playSound(pos.x, pos.y, pos.z, ModSounds.quantumSend, SoundCategory.PLAYERS, .8F, 1F, false);
          for (int i = 0; i < 512; i++) {
            double particleDist = 2;
            Vector3 dest = new Vector3(
                PacketPlayQuantizationEffect.RAND.nextDouble() * 2 - 1,
                PacketPlayQuantizationEffect.RAND.nextDouble() * 2 - 1,
                PacketPlayQuantizationEffect.RAND.nextDouble() * 2 - 1)
                .normalize()
                .multiply(particleDist);
            data = new QuantizationParticleData(dest, time, stack, true);
            mc.world.addParticle(data, pos.x, pos.y, pos.z, 0, 0, 0);
          }
          break;
        case PacketPlayQuantizationEffect.CONSTRUCTION:
          mc.world.playSound(pos.x, pos.y, pos.z, ModSounds.quantumReceive, SoundCategory.PLAYERS, .8F, 1F, false);
          Vector3 rotBase = normal.perpendicular().normalize().multiply(.5);
          for (int i = 0; i < 128; i++) {
            Vector3 roted = rotBase.rotate(PacketPlayQuantizationEffect.RAND.nextDouble() * Math.PI * 2, normal);
            Vector3 p = roted.add(pos);
            data = new QuantizationParticleData(roted.negate(), time, stack, false);
            mc.world.addParticle(data, true, p.x, p.y, p.z, 0, 0, 0);
          }
          break;
        default:
          assert false;
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
