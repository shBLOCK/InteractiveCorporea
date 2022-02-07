package shblock.interactivecorporea.client.requestinghalo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.fonts.TextInputUtil;
import net.minecraft.util.math.MathHelper;
import shblock.interactivecorporea.client.render.RenderUtil;
import shblock.interactivecorporea.common.util.MathUtil;
import vazkii.botania.client.core.handler.ClientTickHandler;

public class HaloSearchBar {
  private static final Minecraft mc = Minecraft.getInstance();

  private boolean searching;
  private String searchString = "";
  private int selectionStart;
  private int selectionEnd;

  private Runnable updateCallback;

  public void render(MatrixStack ms, double radius, double haloHeight) {
    ms.push();
    float textScale = .02F;
    double barHeight = mc.fontRenderer.FONT_HEIGHT * textScale / 2F + .025;
    ms.translate(0, haloHeight + barHeight + .1, 0);
    float alpha = .6F;
    float[] color;
    if (searching) {
      color = MathUtil.hsvaToRGBA((ClientTickHandler.total / 400F) % 1F, 1F, 1F, alpha);
    } else {
      color = new float[]{.5F, .5F, .5F, alpha};
    }
    float[] revCol = MathUtil.revertColor(color);
    double rot = RenderUtil.renderTextOnHaloCentered(
        ms, mc.fontRenderer, searchString, radius - .01, textScale, 0xFFFFFFFF,
        i -> {
          if (searching) {
            if (i >= Math.min(selectionStart, selectionEnd) && i < Math.max(selectionStart, selectionEnd))
              return MathUtil.colorToInt(revCol[0], revCol[1], revCol[2], alpha);
          }
          return 0;
        },
        i -> {
          if (searching) {
            if (i == selectionEnd)
              if ((ClientTickHandler.total / 20) % 1 < .5) {
                return i == searchString.length() ? '_' : '|';
              }
          }
          return (char) 0;
        }
    );
    RenderUtil.renderPartialHalo(
        ms,
        radius,
        Math.max(rot / 2, .1F),
        barHeight,
        .1,
        color[0], color[1], color[2], color[3]
    );
    ms.pop();
  }

  private void fixSelection() {
    if (selectionStart < 0)
      selectionStart = 0;
    if (selectionEnd < 0)
      selectionEnd = 0;
    if (selectionStart > searchString.length())
      selectionStart = searchString.length();
    if (selectionEnd > searchString.length())
      selectionEnd = searchString.length();
  }

  public void backspace() {
    if (!isSearching()) return;

    if (selectionEnd <= searchString.length() && !searchString.isEmpty() && selectionEnd != 0) {
      if (selectionStart == selectionEnd) {
        searchString = searchString.substring(0, selectionEnd - 1) + searchString.substring(selectionEnd);
        selectionEnd--;
        selectionStart--;
        fixSelection();
        updateSearch();
      } else {
        if (deleteSelectedRegion()) {
          updateSearch();
        }
      }
    }
  }

  /**
   * Delete the part of string between start and end (start does not have to be below end)
   * @return if the search string got changed
   */
  private boolean deleteRegion(int start, int end) {
    if (start == end) return false;
    int a = Math.min(start, end);
    int b = Math.max(start, end);
    if (searchString.isEmpty()) return false;
    if (a >= 0 && b <= searchString.length()) {
      searchString = searchString.substring(0, a) + searchString.substring(b);
      return true;
    }
    return false;
  }

  private boolean deleteSelectedRegion() {
    boolean did = deleteRegion(selectionStart, selectionEnd);
    if (did) {
      selectionStart = Math.min(selectionStart, selectionEnd);
      selectionEnd = selectionStart;
      fixSelection();
      return true;
    }
    return false;
  }

  public void delete() {
    if (!isSearching()) return;

    if (selectionStart == selectionEnd) {
      if (selectionStart < searchString.length()) {
        searchString = searchString.substring(0, selectionEnd) + searchString.substring(selectionEnd + 1);
        updateSearch();
      }
    } else {
      if (deleteSelectedRegion()) {
        updateSearch();
      }
    }
  }

  private void insertString(int pos, String insert) {
    searchString = searchString.substring(0, pos) + insert + searchString.substring(pos);
  }

  private void insertStringToSelectionPos(String insert) {
    insertString(selectionEnd, insert);
    selectionEnd += insert.length();
    selectionStart = selectionEnd;
    fixSelection();
  }

  public void inputString(String addString) {
    if (addString.isEmpty()) return;
    if (!isSearching()) return;

    if (selectionStart != selectionEnd) {
      deleteSelectedRegion();
    }
    insertStringToSelectionPos(addString);

    updateSearch();
  }

  public void typeChar(int codePoint, int modifiers) {
    if (!isSearching()) return;

    String addString = "";
    for (char c : Character.toChars(codePoint)) {
      addString += c;
    }

    inputString(addString);
  }

  public void moveSelectionPos(int move, boolean moveStartPos) {
    selectionEnd += move;
    selectionEnd = MathHelper.clamp(selectionEnd, 0, searchString.length());

    if (moveStartPos) {
      selectionStart = selectionEnd;
    }
    fixSelection();
  }

  public void moveToStart() {
    selectionStart = 0;
    selectionEnd = selectionStart;
  }

  public void moveToEnd() {
    selectionStart = searchString.length();
    selectionEnd = selectionStart;
  }

  public void selectAll() {
    selectionStart = 0;
    selectionEnd = searchString.length();
  }

  private void updateSearch() {
    if (updateCallback != null) {
      updateCallback.run();
    }
  }

  public void copy() {
    if (selectionStart != selectionEnd) {
      TextInputUtil.setClipboardText(mc, searchString.substring(Math.min(selectionStart, selectionEnd), Math.max(selectionStart, selectionEnd)));
    }
  }

  public void paste() {
    inputString(TextInputUtil.getClipboardText(mc));
  }

  public void cut() {
    copy();
    deleteSelectedRegion();
    updateSearch();
  }

  public String getSearchString() {
    return searchString;
  }

  public void setSearchString(String str) {
    this.searchString = str;
    fixSelection();
  }

  public void setSearching(boolean searching) {
    this.searching = searching;
  }

  public boolean isSearching() {
    return searching;
  }

  public void setUpdateCallback(Runnable callback) {
    this.updateCallback = callback;
  }
}
