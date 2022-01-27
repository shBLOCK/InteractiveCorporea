package shblock.interactivecorporea.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.client.render.ColoredItemParticleHelper;
import vazkii.botania.common.core.helper.Vector3;

public class QuantizationParticle extends Particle {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final Tessellator tessellator = Tessellator.getInstance();
  private static final BufferBuilder builder = tessellator.getBuffer();

  private final IAnimatedSprite animatedSprite;
  private final TextureAtlasSprite itemSprite;
  private final float itemMinU;
  private final float itemMaxU;
  private final float itemMinV;
  private final float itemMaxV;
  private final float[] itemColor;
  private final boolean quantize;

  protected QuantizationParticle(ClientWorld world, IAnimatedSprite animatedSprite, double x, double y, double z, Vector3 dest, int time, ItemStack stack, boolean quantize) {
    super(world, x, y, z);
    this.motionX = dest.x / time;
    this.motionY = dest.y / time;
    this.motionZ = dest.z / time;
    this.animatedSprite = animatedSprite;
    this.itemSprite = mc.getItemRenderer().getItemModelMesher().getParticleIcon(stack);
    double u = itemSprite.getMaxU() - itemSprite.getMinU();
    double v = itemSprite.getMaxV() - itemSprite.getMinV();
    itemMinU = (float) (itemSprite.getMinU() + rand.nextFloat() * u * .7F + u * .1F);
    itemMaxU = (float) (itemMinU + u * .1F);
    itemMinV = (float) (itemSprite.getMinV() + rand.nextFloat() * v * .7F + u * .1F);
    itemMaxV = (float) (itemMinV + v * .1F);
    this.itemColor = ColoredItemParticleHelper.getRandomColordouble(stack);
    this.quantize = quantize;
    setMaxAge(time);
    canCollide = false;

  }

  @Override
  public void renderParticle(IVertexBuilder buffer, ActiveRenderInfo renderInfo, float pt) {
    Vector3d vector3d = renderInfo.getProjectedView();
    float x = (float) (MathHelper.lerp(pt, this.prevPosX, this.posX) - vector3d.getX());
    float y = (float) (MathHelper.lerp(pt, this.prevPosY, this.posY) - vector3d.getY());
    float z = (float) (MathHelper.lerp(pt, this.prevPosZ, this.posZ) - vector3d.getZ());
    Quaternion quaternion;
    if (this.particleAngle == 0.0F) {
      quaternion = renderInfo.getRotation();
    } else {
      quaternion = new Quaternion(renderInfo.getRotation());
      float f3 = MathHelper.lerp(pt, this.prevParticleAngle, this.particleAngle);
      quaternion.multiply(Vector3f.ZP.rotation(f3));
    }

    double norAge = (double) age / maxAge;
    if (!quantize) {
      norAge = 1 - norAge;
    }

    Vector3f[] rPosItem = new Vector3f[]{
        new Vector3f(-1F, -1F, 0F),
        new Vector3f(-1F, 1F, 0F),
        new Vector3f(1F, 1F, 0F),
        new Vector3f(1F, -1F, 0F)
    };
    Vector3f[] rPosSpark = new Vector3f[]{
        new Vector3f(-1F, -1F, 0F),
        new Vector3f(-1F, 1F, 0F),
        new Vector3f(1F, 1F, 0F),
        new Vector3f(1F, -1F, 0F)
    };

    float scaleItem = quantize ? .1F : .05F;
    float scaleSpark = quantize ? .2F : .1F;

    for(int i = 0; i < 4; ++i) {
      Vector3f itemVec = rPosItem[i];
      itemVec.transform(quaternion);
      itemVec.mul(scaleItem);
      itemVec.add(x, y, z);
      Vector3f sparkVec = rPosSpark[i];
      sparkVec.transform(quaternion);
      sparkVec.mul(scaleSpark);
      sparkVec.add(x, y, z);
    }

    int j = this.getBrightnessForRender(pt);

    float a = (float) ((MathHelper.clamp(1 - norAge, .5, 1) - .5) * 2);

    mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
    builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
    buffer.pos(rPosItem[0].getX(), rPosItem[0].getY(), rPosItem[0].getZ()).color(1F, 1F, 1F, a).tex(itemMaxU, itemMaxV).lightmap(j).endVertex();
    buffer.pos(rPosItem[1].getX(), rPosItem[1].getY(), rPosItem[1].getZ()).color(1F, 1F, 1F, a).tex(itemMaxU, itemMinV).lightmap(j).endVertex();
    buffer.pos(rPosItem[2].getX(), rPosItem[2].getY(), rPosItem[2].getZ()).color(1F, 1F, 1F, a).tex(itemMinU, itemMinV).lightmap(j).endVertex();
    buffer.pos(rPosItem[3].getX(), rPosItem[3].getY(), rPosItem[3].getZ()).color(1F, 1F, 1F, a).tex(itemMinU, itemMaxV).lightmap(j).endVertex();
    tessellator.draw();

    int directionalAge = quantize ? age : maxAge - age;
    TextureAtlasSprite sprite = animatedSprite.get(directionalAge, (int) (maxAge * 1.08));
    float minU = sprite.getMinU();
    float maxU = sprite.getMaxU();
    float minV = sprite.getMinV();
    float maxV = sprite.getMaxV();

    a = (float) Math.sin(norAge * Math.PI);
    float r = itemColor[0];
    float g = itemColor[1];
    float b = itemColor[2];
    a = 1F;

    mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_PARTICLES_TEXTURE);
    builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP);
    buffer.pos(rPosSpark[0].getX(), rPosSpark[0].getY(), rPosSpark[0].getZ()).color(r, g, b, a).tex(maxU, maxV).lightmap(j).endVertex();
    buffer.pos(rPosSpark[1].getX(), rPosSpark[1].getY(), rPosSpark[1].getZ()).color(r, g, b, a).tex(maxU, minV).lightmap(j).endVertex();
    buffer.pos(rPosSpark[2].getX(), rPosSpark[2].getY(), rPosSpark[2].getZ()).color(r, g, b, a).tex(minU, minV).lightmap(j).endVertex();
    buffer.pos(rPosSpark[3].getX(), rPosSpark[3].getY(), rPosSpark[3].getZ()).color(r, g, b, a).tex(minU, maxV).lightmap(j).endVertex();
    tessellator.draw();
  }

  private static final IParticleRenderType RENDER_TYPE = new IParticleRenderType() {
    @Override
    public void beginRender(BufferBuilder bufferBuilder, TextureManager textureManager) {
      RenderSystem.depthMask(true);
      RenderSystem.alphaFunc(GL11.GL_ALWAYS, 0F);
      RenderSystem.defaultAlphaFunc();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
//      RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
    }

    @Override
    public void finishRender(Tessellator tesselator) {
      RenderSystem.defaultBlendFunc();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.depthMask(true);
//      tesselator.draw();
    }

    @Override
    public String toString() {
      return "IC_QUANTIZATION";
    }
  };

  @Override
  public IParticleRenderType getRenderType() {
    return RENDER_TYPE;
  }

  @Override
  public void tick() {
    prevPosX = posX;
    prevPosY = posY;
    prevPosZ = posZ;

    if (age++ >= maxAge) {
      setExpired();
    } else {
      posX += motionX;
      posY += motionY;
      posZ += motionZ;
    }
  }

  @Override
  public boolean shouldCull() {
    return false;
  }
}
