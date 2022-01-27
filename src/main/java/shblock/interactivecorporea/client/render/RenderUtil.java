package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.ints.Int2CharFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.*;
import net.minecraft.util.text.CharacterManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.util.MathUtil;
import shblock.interactivecorporea.common.util.Perlin;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.mixin.AccessorRenderState;

import java.util.Random;
import java.util.function.IntSupplier;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
  private static final RenderType STAR = RenderType.makeType(
      IC.MODID + "star",
      DefaultVertexFormats.POSITION_COLOR,
      GL11.GL_TRIANGLES,
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
        font.renderString(additionalChrStr, 0, 0, color, false, mat, buffers, false, 0, 0xF000F0);
        ms.translate(0, 0, .001);
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
    Matrix4f matrix = mc.gameRenderer.getProjectionMatrix(info, ClientTickHandler.partialTicks, true);
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

    double noisePos = ClientTickHandler.total * .005;

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
}
