package shblock.interactivecorporea.common.tile;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.client.util.RenderTick;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.SPacketPlayQuantizationEffect;
import shblock.interactivecorporea.common.requestinghalo.HaloAttractServerHandler;
import shblock.interactivecorporea.common.util.CISlotPointer;
import shblock.interactivecorporea.common.util.NBTTagHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.api.corporea.ICorporeaResult;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.IManaReceiver;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.block.tile.corporea.TileCorporeaBase;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.impl.corporea.CorporeaItemStackMatcher;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TileItemQuantizationDevice extends TileCorporeaBase implements ITickableTileEntity, IManaReceiver {
  private int mana;

  private final List<Sender> senders = new ArrayList<>();

  public TileItemQuantizationDevice() {
    super(ModTiles.itemQuantizationDevice);
  }

  public int getManaCost(int itemAmount) {
    return itemAmount * ModConfig.COMMON.quantizationConsumption.get();
  }

  public int requestItem(ItemStack stack, Vector3 requestPos, Vector3 normal, ServerPlayerEntity player, ItemStack halo) {
    if (world == null) return 0;
    if (getManaCost(stack.getCount()) > mana) return 0;
    ICorporeaResult result = CorporeaUtil.requestItemNoIntercept(new CorporeaItemStackMatcher(stack, true), stack.getCount(), getSpark(), true);
    List<ItemStack> stacks = result.getStacks();
    if (stacks.isEmpty()) return 0;
    ItemStack resultStack = stacks.get(0);
    for (int i = 1; i < stacks.size(); i++) {
      if (StackHelper.equalItemAndTag(resultStack, stacks.get(i))) {
        resultStack.grow(stacks.get(i).getCount());
      } else {
        world.addEntity(new ItemEntity(world, pos.getX() + .5F, pos.getY() + 1F, pos.getZ() + .5F, stacks.get(i)));
      }
    }
    Vector3 fromPos = new Vector3(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5);
    senders.add(new Sender(resultStack, world, fromPos, requestPos, normal, player, ItemRequestingHalo.isModuleInstalled(halo, HaloModule.MAGNATE)));
    markDirty();
    VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
    consumeMana(getManaCost(resultStack.getCount()));
    return resultStack.getCount();
  }

  @Override
  public void tick() {
    if (world == null) return;
    if (!world.isRemote) {
      boolean dirty = false;
      for (int i = senders.size() - 1; i >= 0; i--) {
        if (senders.get(i).tick()) {
          senders.remove(i);
        }
        dirty = true;
        markDirty();
      }
      if (dirty)
        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
    }
  }

  @Override
  public void readPacketNBT(CompoundNBT cmp) {
    mana = cmp.contains("mana") ? cmp.getInt("mana") : 0;

    senders.clear();
    if (cmp.contains("senders")) {
      ListNBT listNBT = cmp.getList("senders", Constants.NBT.TAG_COMPOUND);
      for (INBT nbt : listNBT) {
        Sender sender = new Sender();
        sender.deserializeNBT((CompoundNBT) nbt);
        senders.add(sender);
      }
    }
  }

  @Override
  public void writePacketNBT(CompoundNBT cmp) {
    cmp.putInt("mana", mana);

    ListNBT listNBT = new ListNBT();
    for (Sender sender : senders) {
      listNBT.add(sender.serializeNBT());
    }
    cmp.put("senders", listNBT);
  }

  public double getLightRelayRenderScale() {
    double scale = 0;
    for (Sender sender : this.senders) {
      double s = sender.getLightRelayRenderScale();
      if (scale < s)
        scale = s;
    }
    return scale;
  }

  @Override
  public boolean isFull() {
    return mana >= getManaCapacity();
  }

  @Override
  public void receiveMana(int receive) {
    mana += receive;
    if (mana >= getManaCapacity()) {
      mana = getManaCapacity();
    }
    markDirty();
  }

  @Override
  public boolean canReceiveManaFromBursts() { return true; }

  @Override
  public int getCurrentMana() {
    return mana;
  }

  public int getManaCapacity() {
    return ModConfig.COMMON.quantizationDeviceManaCapacity.get();
  }

  public int getComparatorLevel() {
    return TilePool.calculateComparatorLevel(mana, getManaCapacity());
  }

  public boolean consumeMana(int consume) {
    if (consume <= mana) {
      mana -= consume;
      markDirty();
      return true;
    }
    return false;
  }

  private static class Sender implements INBTSerializable<CompoundNBT> {
    private int type; // 0: spawn item entity, 1: crafting slot
    private ItemStack stack;
    private World world;
    private Vector3 fromPos;
    private Vector3 pos;
    private Vector3 normal;
    private int time = ModConfig.COMMON.quantizationAnimationSpeed.get() * 3;
    private PlayerEntity player;
    private boolean shouldAttract;
    private CISlotPointer haloSlot;
    private int slot;

    public Sender() { } // for deserializeNBT

    public Sender(int type, ItemStack stack, World world, Vector3 fromPos, Vector3 pos, Vector3 normal, @Nullable PlayerEntity player, boolean shouldAttract, @Nullable CISlotPointer haloSlot, int slot) {
      this.type = type;
      this.stack = stack.copy();
      this.world = world;
      this.fromPos = fromPos;
      this.pos = pos;
      this.normal = normal;
      this.player = player;
      this.shouldAttract = shouldAttract;
      this.haloSlot = haloSlot;
      this.slot = slot;
    }

    /**
     * For spawning item entity
     */
    public Sender(ItemStack stack, World world, Vector3 fromPos, Vector3 pos, Vector3 normal, @Nullable PlayerEntity player, boolean shouldAttract) {
      this(0, stack, world, fromPos, pos, normal, player, shouldAttract, null, -1);
    }

    /**
     * For input to crafting slot
     * @param player used when input failed and spawns item entity
     * @param shouldAttract used when input failed and spawns item entity
     */
    public Sender(ItemStack stack, World world, Vector3 fromPos, Vector3 pos, @Nullable PlayerEntity player, boolean shouldAttract, @Nullable CISlotPointer haloSlot, int slot) {
      this(1, stack, world, fromPos, pos, new Vector3(0, 1, 0), player, shouldAttract, haloSlot, slot);
    }

    /**
     * @return if this sender should be removed (item entity has been summoned)
     */
    public boolean tick() {
      int spd = ModConfig.COMMON.quantizationAnimationSpeed.get();
      if (!world.isRemote) {
        if (time == spd * 3) { // first tick
          ModPacketHandler.sendToPlayersInWorld((ServerWorld) world, new SPacketPlayQuantizationEffect(stack, spd * 2, fromPos, 1));
        } else if (time == spd * 2) {
          ModPacketHandler.sendToPlayersInWorld((ServerWorld) world, new SPacketPlayQuantizationEffect(stack, spd * 2, pos, normal, 1));
        } else if (time == 0) {
          onComplete();
        }
        time--;
      }
      return time < 0;
    }

    private double getLightRelayRenderScale() {
      return 1 - (Math.cos(Math.max(time - 10D - RenderTick.pt, 0D) / 10D * Math.PI) + 1) / 2;
    }

    @Override
    public CompoundNBT serializeNBT() {
      CompoundNBT nbt = new CompoundNBT();
      nbt.putInt("type", type);
      nbt.put("stack", stack.write(new CompoundNBT()));
      NBTTagHelper.putWorld(nbt, "world", world);
      nbt.put("fromPos", NBTTagHelper.putVector3(fromPos));
      nbt.put("pos", NBTTagHelper.putVector3(pos));
      nbt.put("normal", NBTTagHelper.putVector3(normal));
      nbt.putInt("time", time);
      if (player != null)
        nbt.putUniqueId("player", player.getUniqueID());
      if (type == 1) {
        nbt.put("haloSlot", NBTTagHelper.putCISlot(haloSlot));
        nbt.putInt("craftingSlot", slot);
      }

      return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
      this.type = nbt.getInt("type");
      this.stack = ItemStack.read(nbt.getCompound("stack"));
      this.world = NBTTagHelper.getWorld(nbt, "world");
      this.fromPos = NBTTagHelper.getVector3(nbt.getCompound("fromPos"));
      this.pos = NBTTagHelper.getVector3(nbt.getCompound("pos"));
      this.normal = NBTTagHelper.getVector3(nbt.getCompound("normal"));
      this.time = nbt.getInt("time");
      if (nbt.contains("player"))
        player = world.getPlayerByUuid(nbt.getUniqueId("player"));
      if (type == 1) {
        haloSlot = NBTTagHelper.getCISlot(nbt.getCompound("haloSlot"));
        slot = nbt.getInt("craftingSlot");
      }
    }

    private void onComplete() {
      switch (type) {
        case 0: // spawn item entity
          spawnItemEntity();
          break;
        case 1: // to crafting slot
          if (player == null || haloSlot == null || slot == -1)
            spawnItemEntity();

          ItemStack halo = haloSlot.getStack(player);
          if (halo.isEmpty())
            spawnItemEntity();

          ItemStack oldStack = ItemRequestingHalo.getStackInCraftingSlot(halo, slot);
          if (StackHelper.equalItemAndTag(oldStack, stack)) {
            oldStack.grow(stack.getCount());
            ItemRequestingHalo.setStackInCraftingSlot(halo, slot, oldStack);
          } else {
            spawnItemEntity();
          }
          break;
      }
    }

    private void spawnItemEntity() {
      while (stack.getCount() > 0) {
        int cnt = Math.min(stack.getCount(), stack.getMaxStackSize());
        ItemStack spawnStack = stack.copy();
        spawnStack.setCount(cnt);
        stack.shrink(cnt);
        ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, spawnStack);
        entity.setMotion(0, 0, 0);
        world.addEntity(entity);

        if (shouldAttract && (player != null)) {
          HaloAttractServerHandler.addToAttractedItems(player, entity);
        }
      }
    }
  }
}
