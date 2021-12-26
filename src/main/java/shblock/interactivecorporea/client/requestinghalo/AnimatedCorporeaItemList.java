package shblock.interactivecorporea.client.requestinghalo;

import net.minecraft.item.ItemStack;
import shblock.interactivecorporea.common.util.StackHelper;
import shblock.interactivecorporea.common.util.Vec2i;

import java.util.ArrayList;
import java.util.List;

public class AnimatedCorporeaItemList {
  private static float animationLength = 10F;

  private int height = 5;
  private String filter = "";
  private SortMode sortMode = SortMode.DICT;
  private List<ItemStack> stackList;
  private final List<AnimatedItemStack> animatedList = new ArrayList<>();

  public AnimatedCorporeaItemList() {

  }

  public void update(float dt) {
    height = 5;
    for (int i = animatedList.size() - 1; i >= 0; i--) {
      if (animatedList.get(i).update(dt)) {
        animatedList.remove(i);
      }
    }
  }

  public void handleUpdatePacket(List<ItemStack> itemList) {
    stackList = itemList;
    arrange();
  }

  public List<ItemStack> filter(List<ItemStack> list) {
    //TODO: searching
    return new ArrayList<>(list);
  }

  public void sort() {
    animatedList.sort((a, b) -> {
      if (a.isRemoved()) {
        return 1;
      }
      if (b.isRemoved()) {
        return -1;
      }
      return sortMode.getSorter().compare(a.getStack(), b.getStack());
    });

    int y = 0;
    int x = 0;
    for (AnimatedItemStack stack : animatedList) {
      if (stack.isRemoved()) break;
      if (!stack.isNew()) {
        stack.moveTo(new Vec2i(x, y), animationLength);
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
        aniStack.remove(animationLength);
      }
    }

    for (ItemStack stack : list) {
      // any stacks that has not been handled (the stack is newly added)
      AnimatedItemStack aniStack = new AnimatedItemStack(stack);
      aniStack.playFadeIn(animationLength);
      animatedList.add(aniStack);
    }

    sort();
  }

  public void removeAll() {
    for (AnimatedItemStack stack : animatedList) {
      stack.remove(animationLength);
    }
  }

  public List<AnimatedItemStack> getAnimatedList() {
    return animatedList;
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
