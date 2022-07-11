package shblock.interactivecorporea.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import shblock.interactivecorporea.IC;

import java.util.Optional;

public class ModPacketHandler {
  private static final String PROTOCOL_VERSION = "1";
  public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(IC.MODID, "main"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals
  );

  public static void init() {
    int id = 0;
    CHANNEL.registerMessage(id++, CPacketRequestItemListUpdate.class, CPacketRequestItemListUpdate::encode, CPacketRequestItemListUpdate::decode, CPacketRequestItemListUpdate::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketUpdateItemList.class, SPacketUpdateItemList::encode, SPacketUpdateItemList::decode, SPacketUpdateItemList::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, CPacketRequestItem.class, CPacketRequestItem::encode, CPacketRequestItem::decode, CPacketRequestItem::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketPlayQuantizationEffect.class, SPacketPlayQuantizationEffect::encode, SPacketPlayQuantizationEffect::decode, SPacketPlayQuantizationEffect::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, SPacketRequestResult.class, SPacketRequestResult::encode, SPacketRequestResult::decode, SPacketRequestResult::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    CHANNEL.registerMessage(id++, CPacketChangeStackInHaloCraftingSlot.class, CPacketChangeStackInHaloCraftingSlot::encode, CPacketChangeStackInHaloCraftingSlot::decode, CPacketChangeStackInHaloCraftingSlot::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, CPacketDoCraft.class, CPacketDoCraft::encode, CPacketDoCraft::decode, CPacketDoCraft::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, SPacketCraftingState.class, SPacketCraftingState::encode, SPacketCraftingState::decode, SPacketCraftingState::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }

  public static void sendToPlayer(ServerPlayerEntity player, Object message) {
    CHANNEL.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
  }

  public static void sendToPlayersInWorld(ServerWorld world, Object message) {
    for (ServerPlayerEntity player : world.getPlayers()) {
      sendToPlayer(player, message);
    }
  }

  public static void sendToServer(Object message) {
    CHANNEL.sendToServer(message);
  }
}
