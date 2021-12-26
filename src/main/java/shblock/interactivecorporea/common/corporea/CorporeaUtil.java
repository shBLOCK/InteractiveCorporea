package shblock.interactivecorporea.common.corporea;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.network.PacketUpdateItemList;
import shblock.interactivecorporea.common.util.StackHelper;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.ICorporeaNode;
import vazkii.botania.api.corporea.ICorporeaSpark;
import vazkii.botania.common.impl.corporea.CorporeaRequest;

import java.util.ArrayList;
import java.util.List;

public class CorporeaUtil {
  private static final CorporeaAllMatcher ALL_MATCHER = new CorporeaAllMatcher();

  public static void init() {
    CorporeaHelper.instance().registerRequestMatcher(new ResourceLocation(IC.MODID, "all"), CorporeaAllMatcher.class, nbt -> new CorporeaAllMatcher());
  }

  private static void addToListCompacted(List<ItemStack> list, ItemStack stack) {
    for (ItemStack s : list) {
      if (StackHelper.equalItemAndTag(s, stack)) {
        s.grow(stack.getCount());
        return;
      }
    }
    list.add(stack);
  }

  public static List<ItemStack> getAllItemsCompacted(ICorporeaSpark spark) {
    CorporeaHelper ch = CorporeaHelper.instance();
    List<ItemStack> result = new ArrayList<>();
    List<ICorporeaNode> nodes = ch.getNodesOnNetwork(spark);
    CorporeaRequest req = new CorporeaRequest(ALL_MATCHER, Integer.MAX_VALUE);
    for (ICorporeaNode node : nodes) {
      List<ItemStack> c = node.countItems(req);
      c.forEach(stack -> addToListCompacted(result, stack));
    }
    return result;
  }
}
