package shblock.interactivecorporea.client.renderer.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import shblock.interactivecorporea.common.tile.TileItemQuantizationDevice;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TERItemQuantizationDevice extends TileEntityRenderer<TileItemQuantizationDevice> {
  public TERItemQuantizationDevice(TileEntityRendererDispatcher rendererDispatcherIn) {
    super(rendererDispatcherIn);
  }

  @Override
  public void render(TileItemQuantizationDevice tile, float pt, MatrixStack matrixStack, IRenderTypeBuffer buffers, int combinedLight, int combinedOverlay) {

  }
}
