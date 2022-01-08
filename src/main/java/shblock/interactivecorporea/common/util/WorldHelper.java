package shblock.interactivecorporea.common.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.DistExecutor;

import javax.annotation.Nullable;

public class WorldHelper {
  @Nullable
  public static World getWorldFromName(@Nullable RegistryKey<World> key) {
    if (key == null) return null;
    return DistExecutor.unsafeRunForDist(
        () -> () -> ClientSidedCode.getWorldFromName(key),
        () -> () -> ServerSidedCode.getWorldFromName(key)
    );
  }
}
