package shblock.interactivecorporea.common.requestinghalo;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.util.CISlotPointer;
import vazkii.botania.common.core.helper.Vector3;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class HaloCraftingServerHandler {
  private static final List<ServerCraftingInstance> craftingInstances = new ArrayList<>();

  @SubscribeEvent
  public static void onTick(TickEvent.ServerTickEvent event) {

  }

  private static class ServerCraftingInstance {
//    private final Vector3 centerSlotPos;
//    private final double rotation;
    private final Vector3[] slotPositions = new Vector3[9];

    private final ServerPlayerEntity player;
    private final CISlotPointer haloSlot;
    private final boolean doMagnate;

    private int progress = 0;

    public ServerCraftingInstance(Vector3 centerSlotPos, double rotation, double scale, ServerPlayerEntity player, CISlotPointer haloSlot, boolean doMagnate) {
//      this.centerSlotPos = centerSlotPos;
//      this.rotation = rotation;
      for (int i = 0; i < 9; i++) {
        slotPositions[i] = calcSlotWorldPos(i, centerSlotPos, rotation, scale);
      }
      this.player = player;
      this.haloSlot = haloSlot;
      this.doMagnate = doMagnate;
    }

    private static final double SPACING = .6;
    private static Vector3 calcSlotWorldPos(int slot, Vector3 centerSlotPos, double rotation, double scale) {
      int x = -(slot % 3 - 1);
      int y = -(slot / 3 - 1);
      Vector3 pos = new Vector3(x * SPACING * scale, 0, y * SPACING * scale);
      if (slot != 4) {
        pos = pos.rotate(rotation, new Vector3(0, 1, 0));
      }
      return pos.add(centerSlotPos);
    }

//    public boolean tick() {
//
//      progress ++;
//    }
  }
}
