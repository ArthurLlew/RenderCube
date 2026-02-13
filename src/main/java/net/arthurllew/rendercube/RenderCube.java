package net.arthurllew.rendercube;

import net.arthurllew.rendercube.config.Config;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(RenderCube.MODID)
public class RenderCube {
    /**
     * Mod ID.
     */
    public static final String MODID = "rendercube";
    /**
     * Minecraft logger.
     */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mod constructor. Performs basic mod init.
     */
    public RenderCube(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        // Register mod config
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.CONFIG_SPEC);
    }

    /**
     * Mod common setup.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {}
}
