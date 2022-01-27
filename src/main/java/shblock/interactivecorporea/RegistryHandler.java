package shblock.interactivecorporea;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shblock.interactivecorporea.client.particle.QuantizationParticleType;
import shblock.interactivecorporea.client.renderer.item.ISTERBakedModel;
import shblock.interactivecorporea.client.renderer.tile.TERItemQuantizationDevice;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterface;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;
import shblock.interactivecorporea.common.block.BlockItemQuantizationDevice;
import shblock.interactivecorporea.common.block.ModBlocks;
import shblock.interactivecorporea.common.crafting.RequestingHaloAddModuleRecipe;
import shblock.interactivecorporea.common.crafting.RequestingHaloRemoveModuleRecipe;
import shblock.interactivecorporea.common.item.ModItems;
import shblock.interactivecorporea.common.tile.ModTiles;

import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
  private static <V extends IForgeRegistryEntry<V>> void register(IForgeRegistry<V> reg, String name, IForgeRegistryEntry<V> obj) {
    reg.register(obj.setRegistryName(new ResourceLocation(IC.MODID, name)));
  }

  @SubscribeEvent
  public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
    IC.debug("Registering items...");
    IForgeRegistry<Item> reg = event.getRegistry();
    register(reg, "requesting_halo", ModItems.requestingHalo);

    register(reg, BlockItemQuantizationDevice.NAME, ModItems.BlockItems.itemQuantizationDevice);
//        register(reg, "item_wormhole_projector_block", ModItems.BlockItems.itemWormholeProjector);
  }

  @SubscribeEvent
  public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
    IC.debug("Registering blocks...");
    IForgeRegistry<Block> reg = event.getRegistry();
    register(reg, BlockItemQuantizationDevice.NAME, ModBlocks.itemQuantizationDevice);
//        register(reg, "item_wormhole_projector", ModBlocks.itemWormholeProjector);
  }

  private static void registerTile(IForgeRegistry<TileEntityType<?>> reg, Block block, TileEntityType<?> tile) {
    reg.register(tile.setRegistryName(Objects.requireNonNull(block.getRegistryName())));
  }

  @SubscribeEvent
  public static void onTileRegistry(final RegistryEvent.Register<TileEntityType<?>> event) {
    IC.debug("Registering tiles...");
    IForgeRegistry<TileEntityType<?>> reg = event.getRegistry();
    registerTile(reg, ModBlocks.itemQuantizationDevice, ModTiles.itemQuantizationDevice);
  }

  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void onClientSetup(final FMLClientSetupEvent event) {
    ClientRegistry.registerKeyBinding(RequestingHaloInterfaceHandler.KEY_BINDING);
    ClientRegistry.registerKeyBinding(RequestingHaloInterface.KEY_SEARCH);
    ClientRegistry.registerKeyBinding(RequestingHaloInterface.KEY_REQUEST_UPDATE);

    RenderTypeLookup.setRenderLayer(ModBlocks.itemQuantizationDevice, RenderType.getTranslucent());

    ClientRegistry.bindTileEntityRenderer(ModTiles.itemQuantizationDevice, TERItemQuantizationDevice::new);
  }

  @SubscribeEvent
  public static void onParticlesRegister(final RegistryEvent.Register<ParticleType<?>> event) {
    IForgeRegistry<ParticleType<?>> reg = event.getRegistry();
    register(reg, "quantization", QuantizationParticleType.INSTANCE);
  }

  @SubscribeEvent
  public static void onParticleFactoryRegister(ParticleFactoryRegisterEvent event) {
    Minecraft.getInstance().particles.registerFactory(QuantizationParticleType.INSTANCE, QuantizationParticleType.Factory::new);
  }

  @SubscribeEvent
  public static void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
    IForgeRegistry<IRecipeSerializer<?>> reg = event.getRegistry();
    register(reg, "requesting_halo_add_module", RequestingHaloAddModuleRecipe.SERIALIZER);
    register(reg, "requesting_halo_remove_module", RequestingHaloRemoveModuleRecipe.SERIALIZER);
  }

  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void onModelBake(ModelBakeEvent event) {
    IC.debug("Replacing baked models...");
    Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
    new ISTERBakedModel(modelRegistry, ModItems.requestingHalo);
  }

  @SubscribeEvent
  public static void onSoundRegister(RegistryEvent.Register<SoundEvent> event) {
    IC.debug("Registering sound events...");
    IForgeRegistry<SoundEvent> reg = event.getRegistry();
    reg.registerAll(
        ModSounds.haloOpen,
        ModSounds.haloClose,
        ModSounds.haloListUpdate,
        ModSounds.haloSelect,
        ModSounds.haloRequest,
        ModSounds.haloReachEdge,
        ModSounds.quantumSend,
        ModSounds.quantumReceive
    );
  }
}
