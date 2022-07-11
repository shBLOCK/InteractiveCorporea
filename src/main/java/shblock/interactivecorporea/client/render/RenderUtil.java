package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.ints.Int2CharFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.CharacterManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.render.shader.SimpleShaderProgram;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.MathUtil;
import shblock.interactivecorporea.common.util.Perlin;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.mixin.AccessorRenderState;

import java.io.IOException;

import static org.lwjgl.opengl.GL43.*;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
  private static final RenderType STAR = RenderType.makeType(
      IC.MODID + "star",
      DefaultVertexFormats.POSITION_COLOR,
      GL_TRIANGLES,
      256,
      false,
      false,
      RenderType.State.getBuilder().shadeModel(new RenderState.ShadeModelState(true))
          .writeMask(new RenderState.WriteMaskState(true, false))
          .transparency(AccessorRenderState.getLightningTransparency())
          .build(false)
  );

  private static final Minecraft mc = Minecraft.getInstance();

  public static void renderPartialHalo(MatrixStack ms, double radius, double width, double height, double fadeWidth, float r, float g, float b, float alpha) {
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    IVertexBuilder buffer = buffers.getBuffer(ModRenderTypes.halo);

    Matrix4f mat = ms.getLast().getMatrix();
    double fullWidth = width + fadeWidth;
    for (double angle = -fullWidth; angle < fullWidth; angle += Math.PI / 360F) {
      float xp = (float) (Math.sin(angle) * radius);
      float zp = (float) (Math.cos(angle) * radius);

      double minDistToEdge = Math.min(
          Math.abs(fullWidth - angle),
          Math.abs(-fullWidth - angle)
      );
      float currentAlpha = alpha;
      if (minDistToEdge < fadeWidth) {
        currentAlpha *= Math.sin((minDistToEdge / fadeWidth) * (Math.PI / 2));
      }

      buffer.pos(mat, xp, (float) (-height), zp).color(r, g, b, currentAlpha).endVertex();
      buffer.pos(mat, xp, (float) (height), zp).color(r, g, b, currentAlpha).endVertex();
    }
    buffers.finish();
  }

  private static double calcFullTextOnHaloRadians(FontRenderer font, String text, float textScale, double radius) {
    double result = 0;
    CharacterManager cm = font.getCharacterManager();
    for (char c : text.toCharArray()) {
      float width = cm.func_238350_a_(String.valueOf(c)) * textScale;
      result += MathUtil.calcRadiansFromChord(radius, width);
    }
    return result;
  }

  public static double renderTextOnHaloCentered(MatrixStack ms, FontRenderer font, String text, double radius, float textScale, int color, Int2IntFunction bgColorProvider, Int2CharFunction additionalCharProvider) {
    int shadeColor = (color & 16579836) >> 2 | color & -16777216;
    double yOffset = -font.FONT_HEIGHT * textScale / 2D;
    ms.push();
    double fullRot = calcFullTextOnHaloRadians(font, text, textScale, radius);
    ms.rotate(new Quaternion(Vector3f.XP, 180, true));
    ms.rotate(Vector3f.YN.rotation((float) (fullRot / 2 + Math.PI)));
    double rot = 0;
    CharacterManager cm = font.getCharacterManager();
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    char[] chrArray = text.toCharArray();
    for (int i = 0; i < chrArray.length + 1; i++) {
      ms.push();
      ms.rotate(Vector3f.YP.rotation((float) rot));
      String chr = "";
      if (i != chrArray.length) {
        chr = String.valueOf(chrArray[i]);
      }
      float width = cm.func_238350_a_(chr) * textScale;
      ms.translate(0, yOffset, MathUtil.calcChordCenterDistance(radius, width));
      ms.scale(textScale, textScale, textScale);
      Matrix4f mat = ms.getLast().getMatrix();
      if (!chr.isEmpty()) {
        font.renderString(chr, 0, 0, color, false, mat, buffers, false, bgColorProvider.applyAsInt(i), 0xF000F0);
        ms.translate(0, 0, .001);
        font.renderString(chr, 1, 1, shadeColor, false, mat, buffers, false, 0, 0xF000F0);
      }
      char additionalChr = additionalCharProvider.apply(i);
      if (additionalChr != 0) {
        String additionalChrStr = String.valueOf(additionalChr);
        ms.translate(0, 0, .001);
        mat = ms.getLast().getMatrix();
        font.renderString(additionalChrStr, 0, 0, color, false, mat, buffers, false, 0, 0xF000F0);
        ms.translate(0, 0, .001);
        mat = ms.getLast().getMatrix();
        font.renderString(additionalChrStr, 1, 1, shadeColor, false, mat, buffers, false, 0, 0xF000F0);
      }
      ms.pop();

      rot += MathUtil.calcRadiansFromChord(radius, width);
    }
    ms.pop();
    buffers.finish();

    return fullRot;
  }

  public static double renderTextOnHaloCentered(MatrixStack ms, FontRenderer font, String text, double radius, float textScale, int color) {
    return renderTextOnHaloCentered(ms, font, text, radius, textScale, color, i -> 0, i -> (char) 0);
  }

  public static Vector3 worldPosToLocalPos(Vector3 worldPos) {
    ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
    return new Vector3(info.getProjectedView())
        .subtract(worldPos);
  }

  public static Vec2d calcNDC(Vector3 worldCoord) {
    ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
    Vector3 pos = worldPosToLocalPos(worldCoord);
    Vector3d pos3d = new Vector3d(pos.x, pos.y, pos.z)
        .rotateYaw((float) Math.toRadians(info.getYaw() + 180))
        .rotatePitch((float) Math.toRadians(-info.getPitch()));
    Matrix4f matrix = mc.gameRenderer.getProjectionMatrix(info, (float) RenderTick.pt, true);
    Vector4f vec4 = new Vector4f((float) pos3d.x, (float) pos3d.y, (float) pos3d.z, 1F);
    vec4.transform(matrix);
    vec4.perspectiveDivide();
    return new Vec2d(vec4.getX(), vec4.getY());
  }

  public static Vec2d texCoordFromNDC(Vec2d ndc) {
    return ndc.copy().add(1, 1).mul(.5);
  }

  private static final Perlin starPerlin = new Perlin();

  // [Botania Copy] RenderHelper.renderStar() without changing size and with random animation based on perlin noise
  public static void renderPerlinStar(MatrixStack ms, IRenderTypeBuffer buffers, int color, float xScale, float yScale, float zScale, double seed) {
    IVertexBuilder buffer = buffers.getBuffer(STAR);

    float f2 = .15F;

    ms.push();
    ms.scale(xScale, yScale, zScale);

    double noisePos = RenderTick.total * .005;

    for (int i = 0; i < 256; i++) {
      ms.push();
      double z = i * 12.3456789 + seed;
      ms.rotate(new Quaternion(
          (float) (starPerlin.perlin(noisePos, 0, z) * Math.PI * 2),
          (float) (starPerlin.perlin(noisePos, 10, z) * Math.PI * 2),
          (float) (starPerlin.perlin(noisePos, 20, z) * Math.PI * 2),
          false));
//      if (starPerlin.perlin(noisePos, 0, perlinZ) * Math.PI == starPerlin.perlin(noisePos, 100, perlinZ) * Math.PI)
//        System.out.println(starPerlin.perlin(noisePos, 100, perlinZ) * Math.PI);
      float f3 = (float) (starPerlin.perlin(noisePos, 30, z) * 20F + 5F + f2 * 10F);
      float f4 = (float) (starPerlin.perlin(noisePos, 40, z) * 2F + 1F + f2 * 2F);
      float r = ((color & 0xFF0000) >> 16) / 255F;
      float g = ((color & 0xFF00) >> 8) / 255F;
      float b = (color & 0xFF) / 255F;
      Matrix4f mat = ms.getLast().getMatrix();
      Runnable center = () -> buffer.pos(mat, 0, 0, 0).color(r, g, b, 1F).endVertex();
      Runnable[] vertices = {
          () -> buffer.pos(mat, -0.866F * f4, f3, -0.5F * f4).color(0, 0, 0, 0).endVertex(),
          () -> buffer.pos(mat, 0.866F * f4, f3, -0.5F * f4).color(0, 0, 0, 0).endVertex(),
          () -> buffer.pos(mat, 0, f3, f4).color(0, 0, 0, 0).endVertex(),
          () -> buffer.pos(mat, -0.866F * f4, f3, -0.5F * f4).color(0, 0, 0, 0).endVertex()
      };
      RenderHelper.triangleFan(center, vertices);
      ms.pop();
    }

    ms.pop();
  }

  public static void renderFlatItem(MatrixStack ms, ItemStack stack) {
    RenderSystem.pushLightingAttributes();

    ms.push();

//    Matrix4f matrix = ms.getLast().getMatrix().copy();
//    matrix.invert();
//    net.minecraft.client.renderer.RenderHelper.setupLevelDiffuseLighting(matrix);

    ms.scale(1F, 1F, .001F);

    IBakedModel ibakedmodel = mc.getItemRenderer().getItemModelWithOverrides(stack, mc.world, mc.player);
    if (ibakedmodel.isGui3d()) {
      net.minecraft.client.renderer.RenderHelper.setupGui3DDiffuseLighting();
    } else {
      net.minecraft.client.renderer.RenderHelper.setupGuiFlatDiffuseLighting();
    }

    ms.getLast().getNormal().set(Matrix3f.makeScaleMatrix(1F, -1F, 1F));

    IRenderTypeBuffer.Impl buffers = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
    mc.getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GUI, false, ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, ibakedmodel);

    buffers.finish();

    ms.pop();

    net.minecraft.client.renderer.RenderHelper.setupLevelDiffuseLighting(ms.getLast().getMatrix());
    RenderSystem.popAttributes();
  }

  private static final SimpleShaderProgram stipplingShader = new SimpleShaderProgram("stippling", shader -> stipplingAlphaUniformLocation = shader.getUniformLocation("alpha"));
  private static int stipplingAlphaUniformLocation = 0;
  static {
//    try {
//      stipplingShader.load();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }
  /**
   * Apply the stippling effect ("fake" alpha) to your render
   * @param alpha the "fake" alpha level (0~16, 0 is fully transparent, 16 is fully opaque)
   * @param renderer
   */
  public static void applyStippling(int alpha, Runnable renderer) {
    mc.getFramebuffer().enableStencil();

    glEnable(GL_STENCIL_TEST);

    RenderSystem.colorMask(false, false, false, false);
    RenderSystem.depthMask(false);
    RenderSystem.stencilFunc(GL_NEVER, 1, 0xFF);
    RenderSystem.stencilOp(GL_REPLACE, GL_KEEP, GL_KEEP);
    RenderSystem.stencilMask(0xFF);
    RenderSystem.clearStencil(0);
    RenderSystem.clear(GL_STENCIL_BUFFER_BIT, true);

    double width = mc.getFramebuffer().framebufferWidth;
    double height = mc.getFramebuffer().framebufferHeight;
    GlStateManager.disableDepthTest();
    GlStateManager.matrixMode(5889);
    GlStateManager.pushMatrix();
    GlStateManager.loadIdentity();
    GlStateManager.ortho(0.0D, width, height, 0.0D, -1D, 1D);
    GlStateManager.matrixMode(5888);
    GlStateManager.loadIdentity();
    GlStateManager.viewport(0, 0, (int) width, (int) height);
    GlStateManager.enableTexture();
    GlStateManager.disableLighting();
    GlStateManager.disableAlphaTest();

    stipplingShader.use();
    glUniform1i(stipplingAlphaUniformLocation, alpha);
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder builder = tessellator.getBuffer();
    builder.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    builder.pos(0, height, 0).color(0, 0, 0, 255).endVertex();
    builder.pos(width, height, 0).color(0, 0, 0, 255).endVertex();
    builder.pos(width, 0, 0).color(0, 0, 0, 255).endVertex();
    builder.pos(0, 0, 0).color(0, 0, 0, 255).endVertex();
    tessellator.draw();
    stipplingShader.release();

    GlStateManager.matrixMode(5889);
    GlStateManager.popMatrix();
    RenderSystem.colorMask(true, true, true, true);
    RenderSystem.depthMask(true);
    RenderSystem.stencilMask(0x00);
    RenderSystem.stencilFunc(GL_EQUAL, 1, 0xFF);

    renderer.run();

    glDisable(GL_STENCIL_TEST);
  }

  public static void applyStippling(double alpha, Runnable renderer) {
    applyStippling((int) Math.round(alpha * 16), renderer);
  }
}
