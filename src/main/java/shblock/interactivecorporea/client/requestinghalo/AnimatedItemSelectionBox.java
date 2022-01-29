package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.mixin.AccessorRenderState;

import java.awt.*;

public class AnimatedItemSelectionBox {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final ResourceLocation ICON = new ResourceLocation(IC.MODID, "textures/ui/selection_box.png");
  private static final RenderType RENDER_TYPE = RenderType.makeType(
      IC.MODID + "_item_selection_box",
      DefaultVertexFormats.POSITION_COLOR_TEX,
      GL11.GL_QUADS,
      16, false, false,
      RenderType.State.getBuilder()
          .transparency(AccessorRenderState.getTranslucentTransparency())
          .texture(new RenderState.TextureState(ICON, false, false))
          .build(false)
  );

  private AnimatedItemStack target;
  private AnimatedItemStack lastTarget;
  private final Vec2d pos = new Vec2d();
  private float alpha;
  private float requestAnimationTime = -1F;
  private Runnable soundPlayer;

  public AnimatedItemSelectionBox(Runnable soundPlayer) {
    this.soundPlayer = soundPlayer;
  }

  public void setTarget(AnimatedItemStack target) {
    this.target = target;
  }

  public AnimatedItemStack getTarget() {
    return target;
  }

  public void update() {
    double spdA = .1F;
    double spdB = .01F;
    if (target == null) {
      alpha -= alpha * spdA + spdB;
      if (alpha < 0F)
        alpha = 0F;
    } else {
      alpha += (1 - alpha) * spdA + spdB;
      if (alpha > 1F)
        alpha = 1F;
    }

    if (target != null) {
      if (target != lastTarget) {
        soundPlayer.run();
      }

      Vec2d targetPos = target.getPos();
      double spdModifier = Math.sin(MathHelper.clamp(pos.distanceTo(targetPos), 0F, .5F) * Math.PI);
      spdModifier = MathHelper.clamp(spdModifier, 0.1F, 0.5F);
      spdModifier *= ClientTickHandler.delta;
      pos.add(
          (targetPos.x - pos.x) * spdModifier,
          (targetPos.y - pos.y) * spdModifier
      );

      if (target.isRemoved()) {
        target = null;
      }
    }
    lastTarget = target;
  }

  public Vec2d getPos() {
    return pos;
  }

  public void render(MatrixStack ms) {
    ms.push();
    ms.translate(0, 0, -.1);
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    IVertexBuilder buffer = buffers.getBuffer(RENDER_TYPE);
    Color color = Color.getHSBColor(ClientTickHandler.total  / 200F,1F, 1F);
    float r = color.getRed() / 255F;
    float g = color.getGreen() / 255F;
    float b = color.getBlue() / 255F;
    if (requestAnimationTime > 0F) {
      requestAnimationTime -= ClientTickHandler.delta / 5F;
      float aniScale = .25F;
      ms.scale(
          (float) (Math.sin(requestAnimationTime * Math.PI * 2F + Math.PI * 2F) * aniScale + 1F),
          (float) (-Math.sin(requestAnimationTime * Math.PI * 2F) * aniScale + 1F),
          1F
      );
    }
    Matrix4f matrix = ms.getLast().getMatrix();
    buffer.pos(matrix, -.5F, -.5F, 0F).color(r, g, b, alpha).tex(0F, 0F).endVertex();
    buffer.pos(matrix, -.5F, .5F, 0F).color(r, g, b, alpha).tex(0F, 1F).endVertex();
    buffer.pos(matrix, .5F, .5F, 0F).color(r, g, b, alpha).tex(1F, 1F).endVertex();
    buffer.pos(matrix, .5F, -.5F, 0F).color(r, g, b, alpha).tex(1F, 0F).endVertex();
    buffers.finish(RENDER_TYPE);
    ms.pop();
  }

  /**
   * called when the client send the requesting packet to the server (not when the request has been success)
   */
  public void playRequestAnimation() {
    requestAnimationTime = 1F;
  }
}
