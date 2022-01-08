package shblock.interactivecorporea.common.tile;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.network.ModPacketHandler;
import shblock.interactivecorporea.common.network.PacketPlayQuantizationEffect;
import shblock.interactivecorporea.common.util.NBTTagHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.api.corporea.ICorporeaResult;
import vazkii.botania.common.block.tile.corporea.TileCorporeaBase;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.impl.corporea.CorporeaItemStackMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TileItemQuantizationDevice extends TileCorporeaBase implements ITickableTileEntity {
  private static final Random RAND = new Random();
  private final List<Sender> senders = new ArrayList<>();

  public TileItemQuantizationDevice() {
    super(ModTiles.itemQuantizationDevice);
  }

  public int requestItem(ItemStack stack, Vector3 requestPos, Vector3 normal, ServerPlayerEntity player) {
    if (world == null) return 0;
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
    senders.add(new Sender(resultStack, (ServerWorld) world, fromPos, requestPos, normal));
    return resultStack.getCount();
  }

  @Override
  public void tick() {
    if (world == null) return;
    if (!world.isRemote) {
      for (int i = senders.size() - 1; i >= 0; i--) {
        if (senders.get(i).tick()) {
          senders.remove(i);
        }
      }
    }
  }

  @Override
  public void readPacketNBT(CompoundNBT cmp) {
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
    ListNBT listNBT = new ListNBT();
    for (Sender sender : senders) {
      listNBT.add(sender.toNBT());
    }
    cmp.put("senders", listNBT);
  }

  private static class Sender {
    private final ItemStack stack;
    private final ServerWorld world;
    private final Vector3 fromPos;
    private final Vector3 pos;
    private final Vector3 normal;
    private int time = 30;

    public Sender(ItemStack stack, ServerWorld world, Vector3 fromPos, Vector3 pos, Vector3 normal) {
      this.stack = stack.copy();
      this.world = world;
      this.fromPos = fromPos;
      this.pos = pos;
      this.normal = normal;
    }

    /**
     * @return if this sender should be removed (item entity has been summoned)
     */
    public boolean tick() {
      time--;
      if (time == 29) { // first tick
        ModPacketHandler.sendToPlayersInWorld(world, new PacketPlayQuantizationEffect(stack, 20, fromPos));
      } else if (time == 19) {
        ModPacketHandler.sendToPlayersInWorld(world, new PacketPlayQuantizationEffect(stack, 20, pos, normal));
      } else if (time == 0) {
        while (stack.getCount() > 0) {
          int cnt = Math.min(stack.getCount(), stack.getMaxStackSize());
          ItemStack spawnStack = stack.copy();
          spawnStack.setCount(cnt);
          stack.shrink(cnt);
          ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, spawnStack);
          entity.setMotion(0, 0, 0);
          world.addEntity(entity);
        }
      }
      return time < 0;
    }

    public static Sender fromNBT(CompoundNBT nbt) {
      Sender s = new Sender(
          ItemStack.read(nbt.getCompound("stack")),
          (ServerWorld) NBTTagHelper.getWorld(nbt, "world"),
          NBTTagHelper.getVector3(nbt.getCompound("fromPos")),
          NBTTagHelper.getVector3(nbt.getCompound("pos")),
          NBTTagHelper.getVector3(nbt.getCompound("normal"))
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
  }
}
