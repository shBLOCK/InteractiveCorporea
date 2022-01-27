package shblock.interactivecorporea.client.renderer.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.common.util.Perlin;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.tile.RenderTileCorporeaCrystalCube;
import vazkii.botania.client.render.tile.RenderTileCorporeaIndex;

public class ISTERRequestingHalo extends ItemStackTileEntityRenderer {
  private static final Minecraft mc = Minecraft.getInstance();
  private static RenderTileCorporeaIndex corporeaIndexRenderer;
  private static final Perlin rotPerlin = new Perlin();

  @Override
  public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack ms, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay) {
    if (corporeaIndexRenderer == null)
      corporeaIndexRenderer = new RenderTileCorporeaIndex(TileEntityRendererDispatcher.instance);

    ms.push();
    switch (transformType) {
      case FIRST_PERSON_LEFT_HAND:
      case THIRD_PERSON_LEFT_HAND:
      case FIRST_PERSON_RIGHT_HAND:
      case THIRD_PERSON_RIGHT_HAND:
        float s = .75F;
        ms.scale(s, s, s);
        ms.translate(.15, 0, 0);
        break;
    }

    float pt = ClientTickHandler.partialTicks;

    ms.push();
    ms.scale(.5F, .5F, .5F);
    ms.translate(.5, .5, .5);
    corporeaIndexRenderer.render(null, pt, ms, buffers, combinedLight, combinedOverlay);
    ms.pop();

    ms.push();
    ms.translate(.5, .5, .5);
    int color = MathHelper.hsvToRGB((ClientTickHandler.total / 200F) % 1F, 1F, 1F) | 150 << 24;
    RenderUtil.renderPerlinStar(ms, buffers, color, .1F, .1F, .1F, 0);
    ms.pop();

    BlockRendererDispatcher blockRenderer = mc.getBlockRendererDispatcher();
    BlockModelRenderer blockModelRenderer = blockRenderer.getBlockModelRenderer();
    IVertexBuilder buffer = buffers.getBuffer(Atlases.getTranslucentCullBlockType());

    ms.push();
    ms.scale(.5F, .5F, .5F);
    ms.translate(.5, .5, .5);
    ms.translate(.5, .5, .5);
    ms.translate(-.5, -.5, -.5);
    double per = 0.02;
    for (double i = 0; i < 1; i += per) {
      ms.push();
      ms.translate(.5, .5, .5);
      ms.rotate(Vector3f.ZP.rotation((float) (Math.PI * i * 2)));
      ms.translate(-.5, -.5, -.5);
      ms.translate(0, .8, 0);

      ms.translate(.5, .5, .5);
      double r = ClientTickHandler.total * .1;
      ms.rotate(new Quaternion(
          (float) ((i + Math.sin(ClientTickHandler.total / 10) / 5) * Math.PI * 4),
          (float) r,
          (float) (Math.PI * Math.sin(ClientTickHandler.total / 10) * .25),
          false
      ));
      ms.translate(-.5, -.5, -.5);

      blockModelRenderer.renderModel(ms.getLast(), buffer, null, RenderTileCorporeaCrystalCube.cubeModel, 1F, 1F, 1F, combinedLight, combinedOverlay, EmptyModelData.INSTANCE);
      ms.pop();
    }
    ms.pop();

    ms.pop();
  }
}
