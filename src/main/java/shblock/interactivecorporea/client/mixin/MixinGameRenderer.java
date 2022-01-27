package shblock.interactivecorporea.client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shblock.interactivecorporea.client.wormhole.WormholeRenderer;

import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
  @Inject(at = @At(value = "INVOKE", target = "net.minecraft.client.Minecraft.getFramebuffer()Lnet/minecraft/client/shader/Framebuffer;", ordinal = 0), method = "updateCameraAndRender")
  private void injectionApplyWormholeShader(CallbackInfo ci) {
    WormholeRenderer.postProcess();

    // Reset the texture config so that vanilla things can render properly
    RenderSystem.activeTexture(GL_TEXTURE0);
    RenderSystem.enableTexture();
  }
}
