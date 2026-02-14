package net.arthurllew.rendercube;

import net.arthurllew.rendercube.config.Config;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(RenderCube.MODID)
public class RenderCube {
    /**
     * Mod ID.
     */
    public static final String MODID = "rendercube";

    /**
     * Mod init.
     */
    public RenderCube(FMLJavaModLoadingContext context) {
        // Get event bus
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register mod for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        // The mod being absent on the other network side does not cause the client
        // to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(
                        () -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));

        // Register mod config
        context.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    /**
     * Mod common setup.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {}
}
