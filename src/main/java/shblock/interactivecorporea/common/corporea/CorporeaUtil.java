package shblock.interactivecorporea.common.corporea;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import shblock.interactivecorporea.IC;
import shblock.interactivecorporea.common.util.ItemListHelper;
import vazkii.botania.api.corporea.*;
import vazkii.botania.common.impl.corporea.CorporeaRequest;
import vazkii.botania.common.impl.corporea.CorporeaResult;

import java.util.ArrayList;
import java.util.List;

public class CorporeaUtil {
  private static final CorporeaAllMatcher ALL_MATCHER = new CorporeaAllMatcher();

  public static void init() {
    CorporeaHelper.instance().registerRequestMatcher(new ResourceLocation(IC.MODID, "all"), CorporeaAllMatcher.class, nbt -> new CorporeaAllMatcher());
  }

  public static List<ItemStack> getAllItemsCompacted(ICorporeaSpark spark) {
    CorporeaHelper ch = CorporeaHelper.instance();
    List<ItemStack> result = new ArrayList<>();
    List<ICorporeaNode> nodes = ch.getNodesOnNetwork(spark);
    CorporeaRequest req = new CorporeaRequest(ALL_MATCHER, Integer.MAX_VALUE);
    for (ICorporeaNode node : nodes) {
      List<ItemStack> c = node.countItems(req);
      c.forEach(stack -> ItemListHelper.addToListCompacted(result, stack));
    }
    return result;
  }

  // Botania copy from CorporeaHelperImpl: request from corporea without calling interceptors
  public static ICorporeaResult requestItemNoIntercept(ICorporeaRequestMatcher matcher, int itemCount, ICorporeaSpark spark, boolean doit) {
    List<ItemStack> stacks = new ArrayList<>();
    CorporeaRequestEvent event = new CorporeaRequestEvent(matcher, itemCount, spark, !doit);
    if (MinecraftForge.EVENT_BUS.post(event)) {
      return new CorporeaResult(stacks, 0, 0);
    }

    List<ICorporeaNode> nodes = CorporeaHelper.instance().getNodesOnNetwork(spark);

    ICorporeaRequest request = new CorporeaRequest(matcher, itemCount);
    for (ICorporeaNode node : nodes) {
      if (doit) {
        stacks.addAll(node.extractItems(request));
      } else {
        stacks.addAll(node.countItems(request));
      }
    }

    return new CorporeaResult(stacks, request.getFound(), request.getExtracted());
  }
}
