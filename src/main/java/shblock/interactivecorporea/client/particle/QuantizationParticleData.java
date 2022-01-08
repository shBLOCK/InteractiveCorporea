package shblock.interactivecorporea.client.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import vazkii.botania.common.core.helper.Vector3;

public class QuantizationParticleData implements IParticleData {
  public final Vector3 dest;
  public final int time;
  public final ItemStack stack;
  public final boolean quantize;

  public QuantizationParticleData(Vector3 dest, int time, ItemStack stack, boolean quantize) {
    this.dest = dest;
    this.time = time;
    this.stack = stack;
    this.quantize = quantize;
  }

  @Override
  public ParticleType<?> getType() {
    return QuantizationParticleType.INSTANCE;
  }

  @Override
  public void write(PacketBuffer buffer) {
    buffer.writeDouble(dest.x);
    buffer.writeDouble(dest.y);
    buffer.writeDouble(dest.z);
    buffer.writeInt(time);
    buffer.writeItemStack(stack);
    buffer.writeBoolean(quantize);
  }

  @Override
  public String getParameters() {
    return String.format("%s %d %s %s", dest.toString(), time, stack.toString(), quantize);
  }

  public static final IDeserializer<QuantizationParticleData> DESERIALIZER = new IDeserializer<QuantizationParticleData>() {
    @Override
    public QuantizationParticleData deserialize(ParticleType<QuantizationParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
      reader.expect(' ');
      double dx = reader.readDouble();
      reader.expect(' ');
      double dy = reader.readDouble();
      reader.expect(' ');
      double dz = reader.readDouble();
      reader.expect(' ');
      int time = reader.readInt();
      reader.expect(' ');
      ItemParser itemparser = (new ItemParser(reader, false)).parse();
      ItemStack itemstack = (new ItemInput(itemparser.getItem(), itemparser.getNbt())).createStack(1, false);
      reader.expect(' ');
      boolean quantize = reader.readBoolean();

      return new QuantizationParticleData(new Vector3(dx, dy, dz), time, itemstack, quantize);
    }

    @Override
    public QuantizationParticleData read(ParticleType<QuantizationParticleData> particleTypeIn, PacketBuffer buffer) {
      return new QuantizationParticleData(
          new Vector3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
          buffer.readInt(),
          buffer.readItemStack(),
          buffer.readBoolean()
      );
    }
  };
}
