package shblock.interactivecorporea.client.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class RenderTick {
  public static int ticks = 0;
  public static double pt;
  public static double delta;
  public static double total;

  @SubscribeEvent
  public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
    if (event.phase == TickEvent.Phase.START) {
      pt = event.renderTickTime;
      double oldTotal = total;
      total = ticks + pt;
      delta = total - oldTotal;
    }
  }

  @SubscribeEvent
  public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      ticks ++;
    }
  }
}
