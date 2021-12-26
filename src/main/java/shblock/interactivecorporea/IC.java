package shblock.interactivecorporea;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shblock.interactivecorporea.common.corporea.CorporeaUtil;
import shblock.interactivecorporea.common.network.ModPacketHandler;

import java.util.stream.Collectors;

@Mod(IC.MODID)
public class IC {
    public static final String MODID = "interactive_corporea";

    private static final Logger LOGGER = LogManager.getLogger();

    public static final ItemGroup ITEM_GROUP = new ItemGroup("interactive_corporea.itemGroup") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Items.DIAMOND);
        }
    };
    @OnlyIn(Dist.CLIENT)
    public static final String KEY_CATEGORY = "key.interactive_corporea.category";

    public IC() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(RegistryHandler::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ModPacketHandler.init();
        CorporeaUtil.init();
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event) {
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    public static void debug(String message) { LOGGER.debug(message); }
    public static void info(String message) { LOGGER.info(message); }
    public static void warn(String message) { LOGGER.warn(message); }
    public static void error(String message) { LOGGER.error(message); }
}
