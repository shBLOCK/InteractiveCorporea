package shblock.interactivecorporea.common.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;

public class ItemRenderHelper {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final ItemRenderer itemRenderer = mc.getItemRenderer();

  //Vanilla Copy
  public static void renderItemAndEffectIntoGUI(ItemStack stack, int x, int y, MatrixStack ms) {
    if (!stack.isEmpty()) {
      itemRenderer.zLevel += 50.0F;
      IBakedModel bakedmodel = itemRenderer.getItemModelWithOverrides(stack, null, mc.player);
      RenderSystem.pushMatrix();
      mc.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
      mc.textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
      RenderSystem.enableRescaleNormal();
      RenderSystem.enableAlphaTest();
      RenderSystem.defaultAlphaFunc();
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      RenderSystem.translatef((float)x, (float)y, 100.0F + itemRenderer.zLevel);
      RenderSystem.translatef(8.0F, 8.0F, 0.0F);
      RenderSystem.scalef(1.0F, -1.0F, 1.0F);
      RenderSystem.scalef(16.0F, 16.0F, 16.0F);
      IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
      boolean flag = !bakedmodel.isSideLit();
      if (flag) {
        RenderHelper.setupGuiFlatDiffuseLighting();
      }

      itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, ms, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
      irendertypebuffer$impl.finish();
      RenderSystem.enableDepthTest();
      if (flag) {
        RenderHelper.setupGui3DDiffuseLighting();
      }

      RenderSystem.disableAlphaTest();
      RenderSystem.disableRescaleNormal();
      RenderSystem.popMatrix();

      itemRenderer.zLevel -= 50.0F;
    }
  }
}
