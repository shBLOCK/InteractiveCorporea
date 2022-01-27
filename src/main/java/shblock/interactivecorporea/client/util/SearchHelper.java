package shblock.interactivecorporea.client.util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchHelper {
  private static final Minecraft mc = Minecraft.getInstance();

  public static boolean matchItem(ItemStack stack, String[] segments) {
    for (String seg : segments) {
      if (seg.length() == 0) continue;

      String subSeg = seg.substring(1);
      Item item = stack.getItem();
      switch (seg.charAt(0)) {
        case '@': // Mod Name
          String modid = item.getCreatorModId(stack);
          String modName = ModList.get().getModContainerById(modid)
              .map(modContainer -> modContainer.getModInfo().getDisplayName())
              .orElse(modid);
          if (!matchString(modName, subSeg)) {
            return false;
          } else {
            break;
          }
        case '#': // Tooltip
          List<ITextComponent> textComponents = stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
          boolean anyTooltipMatch = false;
          for (ITextComponent textComponent : textComponents) {
            if (matchString(textComponent.getString(), subSeg)) {
              anyTooltipMatch = true;
              break;
            }
          }
          if (!anyTooltipMatch) {
            return false;
          }
          break;
        case '$': // Tag
          Set<ResourceLocation> tags = new HashSet<>(item.getTags());
          if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();
            tags.addAll(block.getTags());
          }
          boolean anyTagMatch = false;
          for (ResourceLocation tag : tags) {
            if (matchString(tag.toString(), subSeg)) {
              anyTagMatch = true;
              break;
            }
          }
          if (!anyTagMatch) {
            return false;
          }
          break;
        case '%': // Creative Tab
          Collection<ItemGroup> groups = item.getCreativeTabs();
          boolean anyTabMatch = false;
          for (ItemGroup group : groups) {
            if (matchString(group.getGroupName().getString(), subSeg)) {
              anyTabMatch = true;
            }
          }
          if (!anyTabMatch) {
            return false;
          }
          break;
        case '&': // Resource Id
          ResourceLocation rid = item.getRegistryName();
          if (!matchString(rid.toString(), subSeg)) {
            return false;
          }
          break;
        default:
          if (!matchString(item.getDisplayName(stack).getString(), subSeg)) {
            return false;
          }
          break;
      }
    }

    return true;
  }

  public static boolean matchString(@Nullable String text, @Nullable String filter) {
    if (text == null || filter == null) {
      return true;
    }
    return text.toLowerCase().contains(filter.toLowerCase());
  }
}
