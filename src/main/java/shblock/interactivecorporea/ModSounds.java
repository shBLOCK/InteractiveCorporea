package shblock.interactivecorporea;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModSounds {
  public static final SoundEvent haloOpen = create("halo.open");
  public static final SoundEvent haloClose = create("halo.close");
  public static final SoundEvent haloListUpdate = create("halo.list_update");
  public static final SoundEvent haloSelect = create("halo.select");
  public static final SoundEvent haloRequest = create("halo.request");
  public static final SoundEvent haloReachEdge = create("halo.reach_edge");
  public static final SoundEvent quantumSend = create("quantum.send");
  public static final SoundEvent quantumReceive = create("quantum.receive");

  private static SoundEvent create(String name) {
    ResourceLocation rl = new ResourceLocation(IC.MODID, name);
    return new SoundEvent(rl).setRegistryName(rl);
  }
}
