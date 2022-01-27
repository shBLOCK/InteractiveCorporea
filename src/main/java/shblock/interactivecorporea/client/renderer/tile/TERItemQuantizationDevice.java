package shblock.interactivecorporea.client.renderer.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.tile.RenderTileCorporeaIndex;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TERItemQuantizationDevice extends TileEntityRenderer<TileItemQuantizationDevice> {
  private static final Minecraft mc = Minecraft.getInstance();
  private static RenderTileCorporeaIndex corporeaIndexRenderer;

  public TERItemQuantizationDevice(TileEntityRendererDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(TileItemQuantizationDevice tile, float pt, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay) {
    if (corporeaIndexRenderer == null)
      corporeaIndexRenderer = new RenderTileCorporeaIndex(TileEntityRendererDispatcher.instance);

    ms.push();
    float scale = (float) (tile.getLightRelayRenderScale() + .2);
    ms.translate(.5, .5, .5);
    ms.scale(scale, scale, scale);
    ms.translate(-.5, -.5, -.5);
    renderLightRelay(ms, pt, buffers);
    ms.pop();

    ms.push();
    ms.translate(.25, Math.sin(ClientTickHandler.total * .1) * .1 + .8, .25);
    ms.scale(.5F, .5F, .5F);
    corporeaIndexRenderer.render(null, pt, ms, buffers, combinedLight, combinedOverlay);
    ms.pop();
  }

  private static TextureAtlasSprite lightRelayIcon = null;

  // [Botania copy] RenderTileLightRelay.render() without access to the tile and the block
  private void renderLightRelay(MatrixStack ms, float pt, IRenderTypeBuffer buffers) {
    if (lightRelayIcon == null)
      lightRelayIcon = MiscellaneousIcons.INSTANCE.lightRelayWorldIcon.getSprite();

    ms.push();
    ms.translate(0.5, 0.3, 0.5);

    double time = ClientTickHandler.ticksInGame + pt;

    float scale = 0.75F;
    ms.scale(scale, scale, scale);

    ms.rotate(mc.getRenderManager().getCameraOrientation());
    ms.rotate(Vector3f.YP.rotationDegrees(180.0F));

    float off = 0.25F;
    ms.translate(0F, off, 0F);
    ms.rotate(Vector3f.ZP.rotationDegrees((float) time));
    ms.translate(0F, -off, 0F);

    IVertexBuilder buffer = buffers.getBuffer(RenderHelper.LIGHT_RELAY);

    float size = lightRelayIcon.getMaxU() - lightRelayIcon.getMinU();
    float pad = size / 8F;
    float f = lightRelayIcon.getMinU() + pad;
    float f1 = lightRelayIcon.getMaxU() - pad;
    float f2 = lightRelayIcon.getMinV() + pad;
    float f3 = lightRelayIcon.getMaxV() - pad;

    float f4 = 1.0F;
    float f5 = 0.5F;
    float f6 = 0.25F;

    Matrix4f mat = ms.getLast().getMatrix();
    int fullbright = 0xF000F0;
    buffer.pos(mat, 0.0F - f5, 0.0F - f6, 0.0F).color(1F, 1F, 1F, 1F).tex(f, f3).lightmap(fullbright).endVertex();
    buffer.pos(mat, f4 - f5, 0.0F - f6, 0.0F).color(1F, 1F, 1F, 1F).tex(f1, f3).lightmap(fullbright).endVertex();
    buffer.pos(mat, f4 - f5, f4 - f6, 0.0F).color(1F, 1F, 1F, 1F).tex(f1, f2).lightmap(fullbright).endVertex();
    buffer.pos(mat, 0.0F - f5, f4 - f6, 0.0F).color(1F, 1F, 1F, 1F).tex(f, f2).lightmap(fullbright).endVertex();

    ms.pop();
  }
}
