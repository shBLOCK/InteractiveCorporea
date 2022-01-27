package shblock.interactivecorporea.client.requestinghalo;

import net.minecraft.item.ItemStack;
import org.lwjgl.system.CallbackI;
import shblock.interactivecorporea.client.util.SearchHelper;
import shblock.interactivecorporea.common.util.StackHelper;
import shblock.interactivecorporea.common.util.Vec2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnimatedCorporeaItemList {
  private static double animationLength = 10F;

  private int height = 5;
  private String filter = "";
  private SortMode sortMode = SortMode.DICT;
  private List<ItemStack> stackList;
  private final List<AnimatedItemStack> animatedList = new ArrayList<>();
  private final Map<Integer, AnimatedItemStack> requestIdMap = new HashMap<>();

  private boolean isFirstUpdate = true;

  public AnimatedCorporeaItemList(int height) {
    this.height = height;
  }

  public void update(double dt) {
    for (int i = animatedList.size() - 1; i >= 0; i--) {
      if (animatedList.get(i).update(dt)) {
        animatedList.remove(i);
      }
    }
  }

  public void tick() {
    for (int i = animatedList.size() - 1; i >= 0; i--) {
      animatedList.get(i).tick();
    }
  }

  public void handleUpdatePacket(List<ItemStack> itemList) {
    stackList = itemList;
    arrange();
  }

  public void setFilter(String filter) {
    this.filter = filter;
  }

  private List<ItemStack> filter(List<ItemStack> list) {
    String[] segments = filter.split(" ");

    return list.stream().filter(
        stack -> SearchHelper.matchItem(stack, segments)
    ).collect(Collectors.toList());
  }

  private void sort() {
    animatedList.sort((a, b) -> {
      if (a.isRemoved() && !b.isRemoved()) {
        return 1;
      }
      if (b.isRemoved() && !a.isRemoved()) {
        return -1;
      }
      if (a.isRemoved() && b.isRemoved()) {
        return 0;
      }
      return sortMode.getSorter().compare(a.getStack(), b.getStack());
    });

    int y = 0;
    int x = 0;
    for (AnimatedItemStack stack : animatedList) {
      if (stack.isRemoved()) break;
      if (!stack.isNew()) {
        stack.moveTo(x, y);
      } else {
        stack.setPos(x, y);
      }
      y++;
      if (y >= height) {
        y = 0;
        x++;
      }
    }
  }

  public void arrange() {
    List<ItemStack> list = filter(stackList);
    for (AnimatedItemStack aniStack : animatedList) {
      ItemStack oldStack = aniStack.getStack();
      boolean found = false;
      for (int i = list.size() - 1; i >= 0; i--) {
        ItemStack stack = list.get(i);

        if (StackHelper.equalItemAndTag(oldStack, stack)) {
          if (aniStack.isRemoved()) {
            aniStack.fadeIn();
          }
          // found the stack with same item type and NBT data in the new list (no changes / amount change)
          if (oldStack.getCount() != stack.getCount()) {
            aniStack.changeAmount(stack.getCount(), animationLength);
          }
          list.remove(i);
          found = true;
          break;
        }
      }
      if (!found) {
        // did not find equal stack in the new list (the stack has been removed)
        aniStack.removeWithAnimation();
      }
    }

    for (ItemStack stack : list) {
      // any stacks that has not been handled (the stack is newly added)
      AnimatedItemStack aniStack = new AnimatedItemStack(stack);
      if (!isFirstUpdate) {
        aniStack.fadeIn();
      }
      animatedList.add(aniStack);
    }

    sort();

    isFirstUpdate = false;
  }

  public void removeAll() {
    for (AnimatedItemStack stack : animatedList) {
      stack.remove();
    }
  }

  private int nextRequestId = 0;

  public int onRequest(AnimatedItemStack aniStack) {
    int id = nextRequestId;
    requestIdMap.put(id, aniStack);
    nextRequestId++;

    return id;
  }

  public void handleRequestResultPacket(int requestId, int successAmount) {
    AnimatedItemStack aniStack = requestIdMap.get(requestId);
    if (aniStack != null && !aniStack.isRemoved()) {
      aniStack.handleRequestResult(successAmount);
    }
  }

  public List<AnimatedItemStack> getAnimatedList() {
    return animatedList;
  }

  public void changeHeight(int delta) {
    height += delta;
    if (height < 1) {
      height = 1;
    }
    if (height > 16) {
      height = 16;
    }
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getHeight() {
    return height;
  }

  public void setSortMode(SortMode mode) {
    sortMode = mode;
  }
}
