package shblock.interactivecorporea.common.network;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.requestinghalo.HaloAttractServerHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NetworkHelper;
import vazkii.botania.common.core.helper.Vector3;

import java.util.function.Supplier;

/**
 * Client-To-Server packet to change the item stack in a requesting halo's crafting slot
 * Put item in the player's hand into the slot if isPut is true, removing the item from the slot (by spawning item entity) otherwise
 */
public class CPacketChangeStackInHaloCraftingSlot {
  private final CISlotPointer haloSlot;
  private final int slot;
  private final boolean isPut;
  private final Vector3 clickPos; // Used to spawn item entities

  public CPacketChangeStackInHaloCraftingSlot(CISlotPointer haloSlot, int slot, boolean isPut, Vector3 clickPos) {
    this.haloSlot = haloSlot;
    this.slot = slot;
    this.isPut = isPut;
    this.clickPos = clickPos;
  }

  public static CPacketChangeStackInHaloCraftingSlot decode(PacketBuffer buf) {
    return new CPacketChangeStackInHaloCraftingSlot(
        NetworkHelper.readCISlotPointer(buf),
        buf.readInt(),
        buf.readBoolean(),
        new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble())
    );
  }

  public void encode(PacketBuffer buf) {
    NetworkHelper.writeCISlotPointer(buf, haloSlot);
    buf.writeInt(slot);
    buf.writeBoolean(isPut);
    buf.writeDouble(clickPos.x);
    buf.writeDouble(clickPos.y);
    buf.writeDouble(clickPos.z);
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      ServerPlayerEntity player = ctx.get().getSender();
      if (player == null) return;
      ItemStack halo = haloSlot.getStack(player);
      if (!(halo.getItem() instanceof ItemRequestingHalo)) return;

      ItemStack putStack = player.getHeldItemMainhand();
      if (isPut) {
        player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        ItemRequestingHalo.tryPutStackInCraftingSlot(
            halo, slot, putStack,
            replacedStack -> {
              ItemEntity ie = new ItemEntity(player.world, clickPos.x, clickPos.y, clickPos.z, replacedStack);
              ie.setMotion(0, 0, 0);
              HaloAttractServerHandler.attractIfHasModule(player, ie, halo);
              player.world.addEntity(ie);
            },
            newAddStack -> player.setHeldItem(Hand.MAIN_HAND, newAddStack)
        );
      } else {
        ItemStack removedStack = ItemRequestingHalo.setStackInCraftingSlot(halo, slot, ItemStack.EMPTY);
        if (!removedStack.isEmpty()) {
          ItemEntity ie = new ItemEntity(player.world, clickPos.x, clickPos.y, clickPos.z, removedStack);
          ie.setMotion(0, 0, 0);
          HaloAttractServerHandler.attractIfHasModule(player, ie, halo);
          player.world.addEntity(ie);
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }
}
