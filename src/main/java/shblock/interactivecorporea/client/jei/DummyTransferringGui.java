package shblock.interactivecorporea.client.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.glfw.GLFW;
import shblock.interactivecorporea.client.render.shader.SimpleShaderProgram;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.client.util.RenderTick;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.io.IOException;

import static org.lwjgl.opengl.GL44.*;

public class DummyTransferringGui extends ContainerScreen<DummyTransferringContainer> {
  private static final SimpleShaderProgram shader = new SimpleShaderProgram("jei_bg", Uniforms::init);

  private static class Uniforms {
    private static int TIME;
    private static int EDGE;
    private static int GUI_SCALE;

    private static void init(SimpleShaderProgram shader) {
      TIME = shader.getUniformLocation("time");
      EDGE = shader.getUniformLocation("edge");
      GUI_SCALE = shader.getUniformLocation("guiScale");
    }
  }

  private double openCloseProgress = 0;
  private boolean closing = false;

  public DummyTransferringGui() {
    super(new DummyTransferringContainer(), Minecraft.getInstance().player.inventory, new StringTextComponent(""));
  }

  private void updateGuiSize() {
    width = Minecraft.getInstance().getMainWindow().getScaledWidth();
    height = Minecraft.getInstance().getMainWindow().getScaledHeight();
    double factor = 1 - (Math.cos(openCloseProgress * Math.PI) + 1) / 2;
    guiLeft = (int) (width / 3 * factor);
    guiTop = 0;
    xSize = (int) (width - (width / 3 * factor) - guiLeft);
    ySize = height;
  }

  public void startClose() {
    closing = true;
  }

  @Override
  public boolean shouldCloseOnEsc() {
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      startClose();
      return true;
    }
    return false;
  }

  @Override
  public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
    if (getContainer().shouldClose || RequestingHaloInterfaceHandler.getInterface() == null || RequestingHaloInterfaceHandler.getInterface().isOpenClose())
      startClose();

    if (!closing) {
      openCloseProgress += RenderTick.delta / 5;
      if (openCloseProgress > 1)
        openCloseProgress = 1;
    } else {
      openCloseProgress -= RenderTick.delta / 5;
      if (openCloseProgress < 0)
        closeScreen();
    }

    updateGuiSize();

    shader.use();
    glUniform1f(Uniforms.TIME, (float) (RenderTick.total / 20));
    glUniform1f(Uniforms.GUI_SCALE, (float) Minecraft.getInstance().getMainWindow().getGuiScaleFactor());

    glUniform1f(Uniforms.EDGE, guiLeft);
    fill(ms, 0, 0, guiLeft, height, 0);

    glUniform1f(Uniforms.EDGE, guiLeft + xSize);
    fill(ms, guiLeft + xSize, 0, width, height, 0);

    shader.release();

    MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this, ms));
  }

  @Override
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) { }
}
