package shblock.interactivecorporea.client.mixin;

import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import shblock.interactivecorporea.client.wormhole.WormholeRenderer;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
  @Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "net.minecraft.client.shader.ShaderGroup.render(F)V", ordinal = 1))
  private void injectionCopyDepth(CallbackInfo ci) {
    WormholeRenderer.copyDepth();
  }
}
