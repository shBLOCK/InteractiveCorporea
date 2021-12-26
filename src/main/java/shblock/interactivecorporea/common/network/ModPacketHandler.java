package shblock.interactivecorporea.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
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
    CHANNEL.registerMessage(id++, PacketRequestItemListUpdate.class, PacketRequestItemListUpdate::encode, PacketRequestItemListUpdate::decode, PacketRequestItemListUpdate::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    CHANNEL.registerMessage(id++, PacketUpdateItemList.class, PacketUpdateItemList::encode, PacketUpdateItemList::decode, PacketUpdateItemList::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }

  public static void sendToPlayer(ServerPlayerEntity player, Object message) {
    CHANNEL.sendTo(message, player.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
  }

  public static void sendToServer(Object message) {
    CHANNEL.sendToServer(message);
  }
}
