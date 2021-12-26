package shblock.interactivecorporea.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

import javax.annotation.Nullable;

public class ClientSidedCode {
  @Nullable
  public static World getWorldFromName(RegistryKey<World> key) {
    if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
      return ServerSidedCode.getWorldFromName(key);
    } else {
      World world = Minecraft.getInstance().world;
      if (world.getDimensionKey().equals(key)) {
        return world;
      } else {
        return null;
      }
    }
  }
}
