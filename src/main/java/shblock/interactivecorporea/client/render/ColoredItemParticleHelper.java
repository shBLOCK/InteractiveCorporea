package shblock.interactivecorporea.client.render;

import mezz.jei.util.MathUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import java.util.*;

public class ColoredItemParticleHelper {
  private static final Map<Item, TextureAtlasSprite[]> CACHE = new HashMap<>();

  private static final Minecraft mc = Minecraft.getInstance();
  private static final Random RAND = new Random();
  private static final Random MODEL_RAND = new Random();
  private static final ItemColors itemColors = mc.getItemColors();
  private static final BlockColors blockColors = mc.getBlockColors();
  private static final ItemModelMesher itemModelMesher = mc.getItemRenderer().getItemModelMesher();
  private static final BlockRendererDispatcher blockRendererDispatcher = mc.getBlockRendererDispatcher();

  static {
    ((IReloadableResourceManager)mc.getResourceManager()).addReloadListener((ISelectiveResourceReloadListener) (resourceManager, resourcePredicate) -> {
      if (resourcePredicate.test(VanillaResourceType.TEXTURES) || resourcePredicate.test(VanillaResourceType.MODELS)) {
        CACHE.clear();
      }
    });
  }

  public static int getRandomColor(TextureAtlasSprite[] sprites, int renderColor) {
    TextureAtlasSprite sprite;
    if (sprites.length != 0) {
      sprite = sprites[RAND.nextInt(sprites.length)];
    } else {
      sprite = mc.getModelManager().getMissingModel().getParticleTexture(EmptyModelData.INSTANCE);
    }
    int color = 0x000000FF;
    for (int i = 0; i < 256; i++) {
      color = sprite.getPixelRGBA(
          RAND.nextInt(sprite.getFrameCount()),
          RAND.nextInt(sprite.getWidth()),
          RAND.nextInt(sprite.getHeight())
      );
      if (NativeImage.getAlpha(color) > 64) {
        int red = (int) ((color & 255 - 1) * (double) (renderColor >> 16 & 255) / 255.0F);
        int green = (int) ((color >> 8 & 255 - 1) * (double) (renderColor >> 8 & 255) / 255.0F);
        int blue = (int) ((color >> 16 & 255 - 1) * (double) (renderColor & 255) / 255.0F);
        red = MathUtil.clamp(red, 0, 255);
        green = MathUtil.clamp(green, 0, 255);
        blue = MathUtil.clamp(blue, 0, 255);
        return ((0xFF) << 24) |
            ((blue & 0xFF) << 16) |
            ((green & 0xFF) << 8) |
            (red & 0xFF);
      }
    }
    return color | (0xFF << 24);
  }

  public static int getRandomColor(ItemStack stack) {
    Item item = stack.getItem();
    TextureAtlasSprite[] sprites = CACHE.get(item);
    if (sprites == null) {
      if (item instanceof BlockItem) {
        sprites = getTextureAtlasSprites(((BlockItem) item).getBlock().getDefaultState());
      } else {
        sprites = getTextureAtlasSprites(item);
      }
      CACHE.put(item, sprites);
    }
    final int renderColor;
    if (item instanceof BlockItem) {
      renderColor = blockColors.getColor(((BlockItem) item).getBlock().getDefaultState(), null, null, 0);
    } else {
      renderColor = itemColors.getColor(stack, 0);
    }
    return getRandomColor(sprites, renderColor);
  }

  public static float[] getRandomColordouble(ItemStack stack) {
    int color = getRandomColor(stack);
    return new float[]{
        (color & 255) / 255F,
        (color >> 8 & 255) / 255F,
        (color >> 16 & 255) / 255F,
        1F
    };
  }

  private static TextureAtlasSprite[] getTextureAtlasSprites(Item item) {
    if (item instanceof BlockItem) {
      return getTextureAtlasSprites(((BlockItem) item).getBlock().getDefaultState());
    }
    IBakedModel model = itemModelMesher.getItemModel(item);
    if (model == null) {
      return new TextureAtlasSprite[0];
    }
    Set<TextureAtlasSprite> result = new HashSet<>();
    List<BakedQuad> quads = model.getQuads(null, null, MODEL_RAND, EmptyModelData.INSTANCE);
    for (BakedQuad quad : quads) {
      result.add(quad.getSprite());
    }
    return result.toArray(new TextureAtlasSprite[0]);
  }

  private static TextureAtlasSprite[] getTextureAtlasSprites(BlockState blockState) {
//    IBakedModel model = blockRendererDispatcher.getModelForState(blockState);
//    Set<TextureAtlasSprite> result = new HashSet<>();
//    List<BakedQuad> quads = model.getQuads(blockState, null, MODEL_RAND, EmptyModelData.INSTANCE);
//    for (BakedQuad quad : quads) {
//      result.add(quad.getSprite());
//    }
//    return result;
    BlockModelShapes blockModelShapes = blockRendererDispatcher.getBlockModelShapes();
    TextureAtlasSprite textureAtlasSprite = blockModelShapes.getTexture(blockState);
    return new TextureAtlasSprite[]{textureAtlasSprite};
  }
}
