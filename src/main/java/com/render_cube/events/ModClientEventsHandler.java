package com.render_cube.events;

import com.mojang.logging.LogUtils;
import com.render_cube.RenderCube;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Paths;

/**
 * Mod client events handler.
 */
@Mod.EventBusSubscriber(modid = RenderCube.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEventsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Listens to resource reload.
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
     * Listens to client setup.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        LOGGER.info("RENDERCUBE CLIENT SETUP");
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        LOGGER.info("RENDERCUBE CLIENT SETUP COMPLETE");
    }
}
