package shblock.interactivecorporea.client.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import shblock.interactivecorporea.IC;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class SimpleShaderProgram implements ISelectiveResourceReloadListener {
  private static final Minecraft mc = Minecraft.getInstance();
  private static final String PREFIX = "shaders/";

  private final ResourceLocation vertexLocation;
  private final ResourceLocation fragmentLocation;
  private final boolean hasVert;

  private int vert = 0;
  private int frag = 0;
  private int program = 0;

  private final Consumer<SimpleShaderProgram> reloadCallback;

  public SimpleShaderProgram(@Nullable String vertLoc, String fragLoc, @Nullable Consumer<SimpleShaderProgram> reloadCallback) {
    if (vertLoc != null) {
      vertexLocation = new ResourceLocation(IC.MODID, PREFIX + vertLoc + ".vert");
      hasVert = true;
    } else {
      vertexLocation = null;
      hasVert = false;
    }
    fragmentLocation = new ResourceLocation(IC.MODID, PREFIX + fragLoc + ".frag");
    this.reloadCallback = reloadCallback;
    IResourceManager resourceManager = mc.getResourceManager();
    if (resourceManager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager) resourceManager).addReloadListener(this);
    }

    try {
      load(mc.getResourceManager());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public SimpleShaderProgram(String fragLoc, @Nullable Consumer<SimpleShaderProgram> reloadCallback) {
    this(null, fragLoc, reloadCallback);
  }

  private void load(IResourceManager resourceManager) throws IOException {
//    if (vert != 0) glDeleteShader(vert);
//    if (frag != 0) glDeleteShader(frag);
//    if (program != 0) glDeleteProgram(program);

    if (hasVert) {
      vert = glCreateShader(GL_VERTEX_SHADER);
      glShaderSource(vert, TextureUtil.readResourceAsString(resourceManager.getResource(vertexLocation).getInputStream()));
      glCompileShader(vert);
      if (glGetShaderi(vert, GL_COMPILE_STATUS) == GL_FALSE) {
        String info = glGetShaderInfoLog(vert);
        vert = 0;
        throw new IOException("Vertex shader " + vertexLocation + " compile failed:\n" + info);
      }
    }

    frag = glCreateShader(GL_FRAGMENT_SHADER);
    glShaderSource(frag, TextureUtil.readResourceAsString(resourceManager.getResource(fragmentLocation).getInputStream()));
    glCompileShader(frag);
    if (glGetShaderi(frag, GL_COMPILE_STATUS) == GL_FALSE) {
      String info = glGetShaderInfoLog(frag);
      frag = 0;
      throw new IOException("Fragment shader " + fragmentLocation + " compile failed:\n" + info);
    }

    program = glCreateProgram();
    if (hasVert) {
      glAttachShader(program, vert);
    }
    glAttachShader(program, frag);
    glLinkProgram(program);
    if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
      String info = glGetProgramInfoLog(program);
      program = 0;
      throw new IOException("Shader " + fragmentLocation + " linking failed:\n" + info);
    }

    reloadCallback.accept(this);
  }

  public int getUniformLocation(String name) {
    return glGetUniformLocation(program, name);
  }

  public void use() {
    glUseProgram(program);
  }

  public void release() {
    glUseProgram(0);
  }

  @Override
  public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
    try {
      load(resourceManager);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
