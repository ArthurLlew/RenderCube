package com.rendercube;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RenderCube.MODID)
public class RenderCube
{
    // The value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "rendercube";

    /**
     * Mod texture atlases directory.
     */
    public static final String TEXTURE_ATLASES_DIR = MODID + "\\" + "texture_atlases";

    /**
     * Mod init.
     */
    public RenderCube()
    {
        // Get event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register mod for server and other game events
        MinecraftForge.EVENT_BUS.register(this);

        // The mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
    }

    /**
     * Listens to mod common setup.
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        Logger LOGGER = LogUtils.getLogger();

        LOGGER.info("RenderCube common setup");
        LOGGER.info("RenderCube common setup complete");
    }
}
