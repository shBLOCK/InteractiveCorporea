package shblock.interactivecorporea.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.IC;
import vazkii.botania.mixin.AccessorRenderState;

public class ModRenderTypes {
  public static RenderType requestingHaloBackground = RenderType.makeType(
      IC.MODID + "_requesting_halo_bg",
      DefaultVertexFormats.POSITION_COLOR,
      GL11.GL_TRIANGLE_STRIP,
      64, false, true,
      RenderType.State.getBuilder()
          .transparency(AccessorRenderState.getTranslucentTransparency())
          .cull(new RenderState.CullState(false))
          .build(false)
  );
}
