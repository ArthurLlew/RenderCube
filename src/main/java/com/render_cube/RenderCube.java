package com.render_cube;

import com.mojang.logging.LogUtils;
import com.render_cube.utils.RenderCubeUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(RenderCube.MODID)
public class RenderCube
{
    // The value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "rendercube";

    /**
     * Mod texture atlases directory.
     */
    public static final String TEXTURE_ATLASES_DIR = MODID + "\\" + "texture_atlases";

    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Items which will all be registered under the MODID namespace

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

        // Register mod's ForgeConfigSpec so that Forge can create and load the config file
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Listens to mod setup event.
     */
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("RENDERCUBE SETUP");

        LOGGER.info("Validating mod directory");
        try {
            RenderCubeUtils.checkModDir();
        }
        catch (IOException e){
            LOGGER.error("Unable to validate mod directory: " + e);
        }

        LOGGER.info("RENDERCUBE SETUP COMPLETE");
    }

    /**
     * Listens to server startup.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("RENDERCUBE server started");
    }
}
