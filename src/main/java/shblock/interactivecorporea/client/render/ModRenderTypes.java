package shblock.interactivecorporea.client.render;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import shblock.interactivecorporea.IC;
import vazkii.botania.mixin.AccessorRenderState;

import static org.lwjgl.opengl.GL44.*;

public class ModRenderTypes {
  public static RenderType halo = RenderType.makeType(
      IC.MODID + "_halo",
      DefaultVertexFormats.POSITION_COLOR,
      GL_QUAD_STRIP,
      64, false, false,
      RenderType.State.getBuilder()
          .cull(new RenderState.CullState(false))
          .transparency(AccessorRenderState.getTranslucentTransparency())
          .build(false)
  );

  public static RenderType craftingBg = RenderType.makeType(
      IC.MODID + "_crafting_bg",
      DefaultVertexFormats.POSITION_COLOR_TEX,
      GL_QUADS,
      16, false, false,
      RenderType.State.getBuilder()
          .cull(new RenderState.CullState(false))
          .transparency(AccessorRenderState.getTranslucentTransparency())
          .build(false)
  );

  public static RenderType craftingSlotBg = RenderType.makeType(
      IC.MODID + "_crafting_slot_bg",
      DefaultVertexFormats.POSITION_COLOR,
      GL_QUADS,
      16, false, false,
      RenderType.State.getBuilder()
          .transparency(AccessorRenderState.getTranslucentTransparency())
          .build(false)
  );
}
