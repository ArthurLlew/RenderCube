package com.render_cube.events;

import com.mojang.logging.LogUtils;
import com.render_cube.RenderCube;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Mod events handler.
 */
@Mod.EventBusSubscriber(modid = RenderCube.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Listens to resource reload event.
     */
    @SubscribeEvent
    public static void reloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Unit>() {

            /**
             * Is invoked before resources load.
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
             * Is invoked after resources load.
             * @param ignored instance of {@link Unit}
             * @param resourceManager instance of {@link ResourceManager}
             * @param profiler instance of {@link ProfilerFiller}
             */
            @Override
            protected void apply(@NotNull Unit ignored,
                                 @NotNull ResourceManager resourceManager,
                                 @NotNull ProfilerFiller profiler) {
                LOGGER.info("Dumping texture atlases.");
                try {
                    RenderCube.dumpTextureMaps();
                } catch (IOException e) {
                    LOGGER.error("Failed to dump texture maps:", e);
                }
            }
        });
    }

    /**
     * Mod setup.
     * @param event common setup event
     */
    @SubscribeEvent
    public static void setup(final FMLCommonSetupEvent event)
    {
        // Some pre-init code
        LOGGER.info("Starting setup");
        LOGGER.info("Setup successful");
    }
}
