package shblock.interactivecorporea.client.wormhole;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.util.Vec2d;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.core.helper.Vector3;

import java.io.IOException;

import static org.lwjgl.opengl.GL44.*;

//@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = IC.MODID)
public class WormholeRenderer {
  private static final Minecraft mc = Minecraft.getInstance();

  private static final int MAX_AMOUNT = 64; // how many wormholes can be rendered at the same time
  private static final int DATA_LENGTH = 7; // the amount of floats it takes to store one wormhole's render data

  private static final Framebuffer swapBuffer = new Framebuffer(
      mc.getFramebuffer().framebufferWidth,
      mc.getFramebuffer().framebufferHeight,
      false,
      Minecraft.IS_RUNNING_ON_MAC
  );
  private static final Framebuffer depthBuffer = new Framebuffer(
      mc.getFramebuffer().framebufferWidth,
      mc.getFramebuffer().framebufferHeight,
      true,
      Minecraft.IS_RUNNING_ON_MAC
  );

  private static boolean didCopyDepth;
//TODO: use our shader class here

//  private static int postVertShader;
  private static int postFragShader;
  private static int postProgram;

  private static int dataTbo;
  private static int dataTex;

  private static boolean enableShader;

  private static boolean initialed;

  private static void init() throws IOException {
    loadShader();

    dataTbo = glGenBuffers();
    glBindBuffer(GL_TEXTURE_BUFFER, dataTbo);
    glBufferData(GL_TEXTURE_BUFFER, MAX_AMOUNT * DATA_LENGTH * 4, GL_DYNAMIC_DRAW);
    glBindBuffer(GL_TEXTURE_BUFFER, 0);

    dataTex = glGenTextures();
    glActiveTexture(GL_TEXTURE31);
    glBindTexture(GL_TEXTURE_BUFFER, dataTex);
    glTexBuffer(GL_TEXTURE_BUFFER, GL_R32F, dataTbo);
    glBindTexture(GL_TEXTURE_BUFFER, 0);
    glActiveTexture(GL_TEXTURE0);

    IC.debug("Wormhole renderer init complete");
  }

  public static void loadShader() throws IOException {
    postFragShader = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(postFragShader, TextureUtil.readResourceAsString(mc.getResourceManager().getResource(new ResourceLocation(IC.MODID, "shaders/wormhole/wormhole.frag")).getInputStream()));
    glCompileShader(postFragShader);
    if (glGetShaderi(postFragShader, GL_COMPILE_STATUS) == GL_FALSE) {
      throw new IOException("Wormhole fragment shader compile failed:\n" + glGetShaderInfoLog(postFragShader));
    }

    postProgram = glCreateProgram();
    glAttachShader(postProgram, postFragShader);
    glLinkProgram(postProgram);
    if (glGetProgrami(postProgram, GL_LINK_STATUS) == GL_FALSE) {
      throw new IOException("Wormhole shader linking failed:\n" + glGetProgramInfoLog(postProgram));
    }

    Uniforms.initUniformLocations(postProgram);

    IC.debug("Wormhole shaders load complete");
  }

  @SubscribeEvent
  public static void onRenderWorldLast(RenderWorldLastEvent event) {
    if (!didCopyDepth) {
      copyDepth();
    }
    didCopyDepth = false;
  }

  public static void copyDepth() {
    if (enableShader) {
      Framebuffer mainFrameBuffer = mc.getFramebuffer();
      depthBuffer.resize(mainFrameBuffer.framebufferWidth, mainFrameBuffer.framebufferHeight, Minecraft.IS_RUNNING_ON_MAC);
      depthBuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
      depthBuffer.func_237506_a_(mainFrameBuffer);
      didCopyDepth = true;
    }
  }

  private static float[] generateData() {
    // all position here is LOCAL POSITION! (relative to the projected view)

    // data structure:
    // 0~2: hole_plane_origin: x, y, z
    // 3~5: hole_plane_normal: x, y, z
    // 6:   radius
    //TODO: generate data
    Wormhole hole = new Wormhole(
//        new Vector3(-768F, 74F, 96F),
        new Vector3(153, 7, -298),
        new Vector3(1F, 0F, 0F),
        .5F
    );

//    hole.radius = (Math.sin(RenderTick.total * .25) + 1) + .25;
    hole.radius = 1;
    Vector3 localPos = RenderUtil.worldPosToLocalPos(hole.pos);
    Vec2d midTexCoord = RenderUtil.texCoordFromNDC(RenderUtil.calcNDC(hole.pos));

    return new float[]{
        (float) localPos.x,
        (float) localPos.y,
        (float) localPos.z,
        (float) hole.normal.x,
        (float) hole.normal.y,
        (float) hole.normal.z,
        (float) hole.radius,
        (float) midTexCoord.x,
        (float) midTexCoord.y
    };
  }

  public static void postProcess() {
    if (!enableShader) return;

    if (!initialed) {
      try {
        init();
        initialed = true;
      } catch (IOException e) {
        e.printStackTrace();
        assert false : e;
      }
    }

    Framebuffer mainFrameBuffer = mc.getFramebuffer();
    swapBuffer.resize(mainFrameBuffer.framebufferWidth, mainFrameBuffer.framebufferHeight, Minecraft.IS_RUNNING_ON_MAC);
    mainFrameBuffer.unbindFramebuffer();
    int width = swapBuffer.framebufferTextureWidth;
    int height = swapBuffer.framebufferTextureHeight;
    RenderSystem.viewport(0, 0, width, height);

    RenderSystem.enableBlend();
    RenderSystem.blendEquation(GL_FUNC_ADD);
    RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    ShaderLinkHelper.func_227804_a_(postProgram);

    RenderSystem.activeTexture(GL_TEXTURE0);
    RenderSystem.enableTexture();
    RenderSystem.bindTexture(mainFrameBuffer.func_242996_f());
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_MIRRORED_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_MIRRORED_REPEAT);
    RenderSystem.glUniform1i(Uniforms.MAIN_SAMPLER, 0);
    RenderSystem.activeTexture(GL_TEXTURE1);
    RenderSystem.enableTexture();
    RenderSystem.bindTexture(depthBuffer.func_242997_g());
    RenderSystem.glUniform1i(Uniforms.MAIN_DEPTH_SAMPLER, 1);

    glActiveTexture(GL_TEXTURE31);
    glBindBuffer(GL_TEXTURE_BUFFER, dataTbo);
    glBufferSubData(GL_TEXTURE_BUFFER, 0, generateData());
    glBindTexture(GL_TEXTURE_BUFFER, dataTex);
    glTexBuffer(GL_TEXTURE_BUFFER, GL_R32F, dataTbo);
    RenderSystem.glUniform1i(Uniforms.DATA, 31);

    glUniform2f(Uniforms.SCREEN_SIZE, width, height);
    glUniform1f(Uniforms.TIME, (float) RenderTick.total);

    applyShaderProjectionData();

    swapBuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
    swapBuffer.bindFramebuffer(false);
    RenderSystem.depthFunc(GL_ALWAYS);

    BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
    bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.POSITION);
    bufferBuilder.pos(0, 0, 500).endVertex();
    bufferBuilder.pos(0, height, 500).endVertex();
    bufferBuilder.pos(width, height, 500).endVertex();
    bufferBuilder.pos(width, 0, 500).endVertex();
    bufferBuilder.finishDrawing();
    WorldVertexBufferUploader.draw(bufferBuilder);

    GlStateManager.bindFramebuffer(GL_READ_FRAMEBUFFER, swapBuffer.framebufferObject);
    GlStateManager.bindFramebuffer(GL_DRAW_FRAMEBUFFER, mainFrameBuffer.framebufferObject);
    GlStateManager.blitFramebuffer(
        0, 0, swapBuffer.framebufferWidth, swapBuffer.framebufferHeight,
        0, 0, mainFrameBuffer.framebufferWidth, mainFrameBuffer.framebufferHeight,
        GL_COLOR_BUFFER_BIT, GL_NEAREST
    );
    GlStateManager.bindFramebuffer(GL_FRAMEBUFFER, 0);

    RenderSystem.depthFunc(GL_LEQUAL);

    ShaderLinkHelper.func_227804_a_(0);
    RenderSystem.activeTexture(GL_TEXTURE0);
    RenderSystem.disableTexture();
    RenderSystem.bindTexture(0);
    RenderSystem.activeTexture(GL_TEXTURE1);
    RenderSystem.disableTexture();
    RenderSystem.bindTexture(0);

    swapBuffer.unbindFramebuffer();
    mainFrameBuffer.unbindFramebufferTexture();
  }

  private static void applyShaderProjectionData() {
    ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
    double aspectRatio = (double) mc.getMainWindow().getFramebufferWidth() / (double) mc.getMainWindow().getFramebufferHeight();
    double fov = mc.gameRenderer.getFOVModifier(info, (float) RenderTick.pt, true);
    fov = Math.toRadians(fov) / 2;
    Vector3d dir = Vector3d.fromPitchYaw(
        info.getPitch() * .9999F,
        info.getYaw() * .9999F
    );
    Vector3 dir1 = new Vector3(dir);
    Vector3 perpendicularX = dir1.yCrossProduct().normalize();
    Vector3 perpendicularY = dir1.crossProduct(perpendicularX).normalize();

    glUniform1f(Uniforms.ASPECT_RATIO, (float) aspectRatio);
    glUniform3f(Uniforms.MID_DIRECTION, (float) dir.x, (float) dir.y, (float) dir.z);
    glUniform1f(Uniforms.FOV, (float) fov);
    glUniform3f(Uniforms.PERPENDICULAR_X, (float) perpendicularX.x, (float) perpendicularX.y, (float) perpendicularX.z);
    glUniform3f(Uniforms.PERPENDICULAR_Y, (float) perpendicularY.x, (float) perpendicularY.y, (float) perpendicularY.z);
    glUniform1f(Uniforms.FAR_PLANE, mc.gameRenderer.getFarPlaneDistance());
  }

  public static boolean isShaderEnabled() {
    return enableShader;
  }

  public static void setShaderEnabled(boolean enable) {
    enableShader = enable;
  }

  private static class Uniforms {
    private static int MAIN_SAMPLER;
    private static int MAIN_DEPTH_SAMPLER;
    private static int DATA;

    private static int SCREEN_SIZE;
    private static int TIME;

    private static int ASPECT_RATIO;
    private static int MID_DIRECTION;
    private static int FOV;
    private static int PERPENDICULAR_X;
    private static int PERPENDICULAR_Y;
    private static int FAR_PLANE;

    private static void initUniformLocations(int program) {
      MAIN_SAMPLER = glGetUniformLocation(program, "mainSampler");
      MAIN_DEPTH_SAMPLER = glGetUniformLocation(program, "mainDepthSampler");
      DATA = glGetUniformLocation(program, "data");

      SCREEN_SIZE = glGetUniformLocation(program, "screenSize");
      TIME = glGetUniformLocation(program, "time");

      ASPECT_RATIO = glGetUniformLocation(program, "aspectRatio");
      MID_DIRECTION = glGetUniformLocation(program, "midDirection");
      FOV = glGetUniformLocation(program, "fov");
      PERPENDICULAR_X = glGetUniformLocation(program, "perpendicularX");
      PERPENDICULAR_Y = glGetUniformLocation(program, "perpendicularY");
      FAR_PLANE = glGetUniformLocation(program, "farPlane");
    }
  }
}
