package shblock.interactivecorporea.common.requestinghalo;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.core.helper.MathHelper;
import vazkii.botania.common.core.helper.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = IC.MODID)
public class HaloAttractServerHandler {
  private static final Map<PlayerEntity, List<ItemEntity>> attractedItems = new HashMap<>();

  @SubscribeEvent
  public static void onTick(TickEvent.ServerTickEvent event) {
    for (Map.Entry<PlayerEntity, List<ItemEntity>> entry : attractedItems.entrySet()) {
      PlayerEntity player = entry.getKey();
      List<ItemEntity> list = entry.getValue();
      if (player.isAlive()) {
        Vector3 pos = new Vector3(player.getPosX(), player.getPosY() + .75, player.getPosZ());
        for (int i = list.size() - 1; i >= 0; i--) {
          ItemEntity item = list.get(i);
          if (!item.isAlive()) {
            list.remove(i);
            continue;
          }

          doAttract(pos, item);
        }
      }
    }
  }

  private static void doAttract(Vector3 pos, ItemEntity item) {
    MathHelper.setEntityMotionFromVector(item, pos, 0.3F);
    item.velocityChanged = true;

    ServerWorld world = (ServerWorld) item.world;
    boolean red = item.world.rand.nextBoolean();
    float r = red ? 1F : 0F;
    float b = red ? 0F : 1F;
    world.spawnParticle(
        SparkleParticleData.sparkle(3F, r, 0, b, 10),
        item.getPosX(),
        item.getPosY() + .2,
        item.getPosZ(),
        1, .1, .1, .1, 1F
    );
  }

  public static void addToAttractedItems(PlayerEntity player, ItemEntity item) {
    List<ItemEntity> list = attractedItems.computeIfAbsent(player, k -> new ArrayList<>());
    list.add(item);
  }

  /**
   * @return if the item was attracted (if the halo has the magnate module)
   */
  public static boolean attractIfHasModule(PlayerEntity player, ItemEntity item, ItemStack halo) {
    if (ItemRequestingHalo.isModuleInstalled(halo, HaloModule.MAGNATE)) {
      addToAttractedItems(player, item);
      return true;
    } else {
      return false;
    }
  }
}
