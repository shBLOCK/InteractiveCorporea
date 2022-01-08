package shblock.interactivecorporea.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.IC;
import vazkii.botania.mixin.AccessorRenderState;

public class ModRenderTypes {
  public static RenderType halo = RenderType.makeType(
      IC.MODID + "halo",
      DefaultVertexFormats.POSITION_COLOR,
      GL11.GL_QUAD_STRIP,
      64, false, false,
      RenderType.State.getBuilder()
          .cull(new RenderState.CullState(false))
          .transparency(AccessorRenderState.getTranslucentTransparency())
          .build(false)
  );
}
