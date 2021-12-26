package shblock.interactivecorporea;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shblock.interactivecorporea.client.requestinghalo.RequestingHaloInterfaceHandler;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class RegistryHandler {
    private static <V extends IForgeRegistryEntry<V>> void register(IForgeRegistry<V> reg, String name, IForgeRegistryEntry<V> obj) {
        reg.register(obj.setRegistryName(new ResourceLocation(IC.MODID, name)));
    }

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
        IC.debug("Registering blocks...");
    }

    @SubscribeEvent
    public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
        IC.debug("Registering items...");

        IForgeRegistry<Item> reg = event.getRegistry();

        register(reg, "requesting_halo", ModItems.requestingHalo);
    }

    public static void onClientSetup(final FMLClientSetupEvent event) {
        ClientRegistry.registerKeyBinding(RequestingHaloInterfaceHandler.KEY_BINDING);
    }
}
