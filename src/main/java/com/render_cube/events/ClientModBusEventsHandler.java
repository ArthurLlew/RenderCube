package com.render_cube.events;

import com.mojang.logging.LogUtils;
import com.render_cube.RenderCube;
import com.render_cube.utils.FileWriters;
import com.render_cube.utils.KeyBinding;
import com.render_cube.utils.RenderCubeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;

@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEventsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Client setup.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) throws IOException {
        LOGGER.info("RENDERCUBE CLIENT SETUP");

        LOGGER.info("Validating " + RenderCube.MODID + " directory");
        try {
            RenderCubeUtils.checkModDir();
        }
        catch (IOException e){
            LOGGER.error("Failed to validate " + RenderCube.MODID + " directory");
            throw e;
        }

        LOGGER.info("RENDERCUBE CLIENT SETUP COMPLETE");
    }

    /**
     * Adds extra logic to resource reload event.
     */
    @SubscribeEvent
    public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Unit>() {

            /**
             * Is invoked before resources are loaded.
             * @param resourceManager instance of {@link ResourceManager}
             * @param profiler instance of {@link ProfilerFiller}
             * @return instance of {@link Unit}
             */
            @Override
            protected @NotNull Unit prepare(@NotNull ResourceManager resourceManager,
                                            @NotNull ProfilerFiller profiler) {
                return Unit.INSTANCE;
            }

            /**
             * Is invoked after resources are loaded.
             * @param ignored instance of {@link Unit}
             * @param resourceManager instance of {@link ResourceManager}
             * @param profiler instance of {@link ProfilerFiller}
             */
            @Override
            protected void apply(@NotNull Unit ignored,
                                 @NotNull ResourceManager resourceManager,
                                 @NotNull ProfilerFiller profiler) {
                LOGGER.info("Dumping texture atlases");
                // Dump textures via texture manager
                Minecraft.getInstance().getTextureManager().dumpAllSheets(Paths.get(RenderCube.TEXTURE_ATLASES_DIR));
                LOGGER.info("Finished dumping texture atlases");
            }
        });
    }

    /**
     * Registers custom key bindings.
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(KeyBinding.RENDER_REGION_KEY);
    }
}
