package shblock.interactivecorporea.client.renderer.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ISTERBakedModel implements IBakedModel {
  private final IBakedModel oldModel;

  public ISTERBakedModel(Map<ResourceLocation, IBakedModel> modelRegistry, Item item) {
    if (item.getRegistryName() == null) {
      throw new RuntimeException("Item not registered! Item:" + item);
    }
    this.oldModel = modelRegistry.get(new ModelResourceLocation(item.getRegistryName(), "inventory"));
    if (oldModel == null)
      throw new RuntimeException("Can't find item model! Item:" + item.getRegistryName());
    modelRegistry.put(new ModelResourceLocation(item.getRegistryName(), "inventory"), this);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) { return oldModel.getQuads(state, side, rand); }

  @Nonnull
  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) { return oldModel.getQuads(state, side, rand, extraData); }

  @Override
  public boolean isAmbientOcclusion() { return oldModel.isAmbientOcclusion(); }

  @Override
  public boolean isGui3d() { return oldModel.isGui3d(); }

  @Override
  public boolean isSideLit() { return oldModel.isSideLit(); }

  @Override
  public boolean isBuiltInRenderer() { return true; }

  @Override
  public TextureAtlasSprite getParticleTexture() { return oldModel.getParticleTexture(); }

  @Override
  public ItemOverrideList getOverrides() { return oldModel.getOverrides(); }

  @Override
  public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
    return this;
  }
}
