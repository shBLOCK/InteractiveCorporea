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
import shblock.interactivecorporea.ModConfig;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.item.HaloModule;
import shblock.interactivecorporea.common.item.ItemRequestingHalo;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.PacketPlayQuantizationEffect;
import shblock.interactivecorporea.common.requestinghalo.HaloServerHandler;
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
import java.util.Random;

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
        senders.add(Sender.fromNBT((CompoundNBT) nbt));
      }
    }
  }

  @Override
  public void writePacketNBT(CompoundNBT cmp) {
    cmp.putInt("mana", mana);

    ListNBT listNBT = new ListNBT();
    for (Sender sender : senders) {
      listNBT.add(sender.toNBT());
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

  private static class Sender {
    private final ItemStack stack;
    private final World world;
    private final Vector3 fromPos;
    private final Vector3 pos;
    private final Vector3 normal;
    private int time = ModConfig.COMMON.quantizationAnimationSpeed.get() * 3;
    private final PlayerEntity player;
    private final boolean shouldAttract;

    public Sender(ItemStack stack, World world, Vector3 fromPos, Vector3 pos, Vector3 normal, @Nullable PlayerEntity player, boolean shouldAttract) {
      this.stack = stack.copy();
      this.world = world;
      this.fromPos = fromPos;
      this.pos = pos;
      this.normal = normal;
      this.player = player;
      this.shouldAttract = shouldAttract;
    }

    /**
     * @return if this sender should be removed (item entity has been summoned)
     */
    public boolean tick() {
      int spd = ModConfig.COMMON.quantizationAnimationSpeed.get();
      if (!world.isRemote) {
        if (time == spd * 3) { // first tick
          ModPacketHandler.sendToPlayersInWorld((ServerWorld) world, new PacketPlayQuantizationEffect(stack, spd * 2, fromPos));
        } else if (time == spd * 2) {
          ModPacketHandler.sendToPlayersInWorld((ServerWorld) world, new PacketPlayQuantizationEffect(stack, spd * 2, pos, normal));
        } else if (time == 0) {
          while (stack.getCount() > 0) {
            int cnt = Math.min(stack.getCount(), stack.getMaxStackSize());
            ItemStack spawnStack = stack.copy();
            spawnStack.setCount(cnt);
            stack.shrink(cnt);
            ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, spawnStack);
            entity.setMotion(0, 0, 0);
            world.addEntity(entity);

            if (shouldAttract && (player != null)) {
              HaloServerHandler.addAttractItem(player, entity);
            }
          }
        }
        time--;
      }
      return time < 0;
    }

    public static Sender fromNBT(CompoundNBT nbt) {
      Sender s = new Sender(
          ItemStack.read(nbt.getCompound("stack")),
          NBTTagHelper.getWorld(nbt, "world"),
          NBTTagHelper.getVector3(nbt.getCompound("fromPos")),
          NBTTagHelper.getVector3(nbt.getCompound("pos")),
          NBTTagHelper.getVector3(nbt.getCompound("normal")),
          null, // Store this is unnecessary
          false // Store this is unnecessary
      );
      s.time = nbt.getInt("time");
      return s;
    }

    public CompoundNBT toNBT() {
      CompoundNBT nbt = new CompoundNBT();
      nbt.put("stack", stack.write(new CompoundNBT()));
      NBTTagHelper.putWorld(nbt, "world", world);
      nbt.put("fromPos", NBTTagHelper.putVector3(fromPos));
      nbt.put("pos", NBTTagHelper.putVector3(pos));
      nbt.put("normal", NBTTagHelper.putVector3(normal));
      nbt.putInt("time", time);
      return nbt;
    }

    private double getLightRelayRenderScale() {
      return 1 - (Math.cos(Math.max(time - 10D - ClientTickHandler.partialTicks, 0D) / 10D * Math.PI) + 1) / 2;
    }
  }
}
