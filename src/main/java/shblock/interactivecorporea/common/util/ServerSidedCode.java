package shblock.interactivecorporea.common.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public class ServerSidedCode {
  @Nullable
  public static World getWorldFromName(RegistryKey<World> key) {
    return ServerLifecycleHooks.getCurrentServer().getWorld(key);
  }
}
