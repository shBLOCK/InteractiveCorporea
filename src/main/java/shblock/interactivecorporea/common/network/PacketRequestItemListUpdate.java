package shblock.interactivecorporea.common.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;
import shblock.interactivecorporea.common.util.WorldHelper;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.ICorporeaSpark;

import java.util.List;
import java.util.function.Supplier;

public class PacketRequestItemListUpdate {
  private final CISlotPointer slot;

  public PacketRequestItemListUpdate(CISlotPointer slot) {
    this.slot = slot;
  }

  public static PacketRequestItemListUpdate decode(PacketBuffer buf) {
    return new PacketRequestItemListUpdate(NetworkHelper.readCISlotPointer(buf));
  }

  public void encode(PacketBuffer buf) {
    NetworkHelper.writeCISlotPointer(buf, slot);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      //TODO: add checks to prevent lagging exploits
      ServerPlayerEntity player = ctx.get().getSender();
      if (player == null) return;
      ItemStack stack = slot.getStack(player);
      if (!(stack.getItem() instanceof ItemRequestingHalo)) return;
      GlobalPos pos = ItemRequestingHalo.getBoundIndexPosition(stack);
      if (pos == null) return;
      ICorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(WorldHelper.getWorldFromName(pos.getDimension()), pos.getPos());
      if (spark == null) return;
      List<ItemStack> result = CorporeaUtil.getAllItemsCompacted(spark);
      ModPacketHandler.sendToPlayer(player, new PacketUpdateItemList(result));
      IC.debug("Update item list packet sent to player: " + player.getGameProfile().getName());
    });
    ctx.get().setPacketHandled(true);
  }
}
