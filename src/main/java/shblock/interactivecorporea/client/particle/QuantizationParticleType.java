package shblock.interactivecorporea.client.particle;

import com.mojang.serialization.Codec;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleType;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nullable;

public class QuantizationParticleType extends ParticleType<QuantizationParticleData> {
  public static final QuantizationParticleType INSTANCE = new QuantizationParticleType();

  public QuantizationParticleType() {
    super(false, QuantizationParticleData.DESERIALIZER);
  }

  @Override
  public Codec<QuantizationParticleData> func_230522_e_() {
    return Codec.unit(new QuantizationParticleData(Vector3.ZERO, 0, ItemStack.EMPTY, false));
  }

  public static class Factory implements IParticleFactory<QuantizationParticleData> {
    private final IAnimatedSprite animatedSprite;

    public Factory(IAnimatedSprite animatedSprite) {
      this.animatedSprite = animatedSprite;
    }

    @Nullable
    @Override
    public Particle makeParticle(QuantizationParticleData data, ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      return new QuantizationParticle(world, animatedSprite, x, y, z, data.dest, data.time, data.stack, data.quantize);
    }
  }
}
