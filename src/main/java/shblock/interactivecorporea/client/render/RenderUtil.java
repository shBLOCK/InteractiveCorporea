package shblock.interactivecorporea.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import shblock.interactivecorporea.client.util.MathUtil;

public class RenderUtil {
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

  public static void renderTextOnHaloCentered(MatrixStack ms, FontRenderer font, String text, double radius, float textScale, int color) {
    int shadeColor = (color & 16579836) >> 2 | color & -16777216;
    double yOffset = -font.FONT_HEIGHT * textScale / 2D;
    ms.push();
    float fullWidth = font.getCharacterManager().func_238350_a_(text) * textScale;
    ms.rotate(new Quaternion(Vector3f.XP, 180, true));
    ms.rotate(Vector3f.YN.rotation((float) ((float) MathUtil.calcRadiansFromChord(radius, fullWidth) / 2F + Math.PI)));
    double rot = 0;
    IRenderTypeBuffer.Impl buffers = mc.getRenderTypeBuffers().getBufferSource();
    for (char c : text.toCharArray()) {
      ms.push();
      ms.rotate(Vector3f.YP.rotation((float) rot));
      String chr = String.valueOf(c);
      float width = font.getCharacterManager().func_238350_a_(chr) * textScale;
      ms.translate(0, yOffset, MathUtil.calcChordCenterDistance(radius, width));
      ms.scale(textScale, textScale, textScale);
      Matrix4f mat = ms.getLast().getMatrix();
      font.renderString(chr, 0, 0, color, false, mat, buffers, false, 0, 0xF000F0); //TODO: shadow?
      ms.translate(0, 0, .001);
      font.renderString(chr, 1, 1, shadeColor, false, mat, buffers, false, 0, 0xF000F0); //TODO: shadow?
      ms.pop();

      rot += MathUtil.calcRadiansFromChord(radius, width);
    }
    ms.pop();
    buffers.finish();
  }
}
