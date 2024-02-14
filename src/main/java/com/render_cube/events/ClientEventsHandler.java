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

public class ClientEventsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
    public static class ClientForgeEvents{
        /**
         * Key pressing logic.
         */
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event){
            if(KeyBinding.RENDER_REGION_KEY.consumeClick()){
                try (FileWriters fileWriters = new FileWriters()){
                    LocalPlayer player = Minecraft.getInstance().player;

                    // Min and max coordinates over each axes
                    int region_min_x = Math.min(player.getBlockX() - 64, player.getBlockX() + 64);
                    int region_max_x = Math.max(player.getBlockX() - 64, player.getBlockX() + 64);
                    int region_min_y = Math.min(player.getBlockY() - 64, player.getBlockY() + 64);
                    int region_max_y = Math.max(player.getBlockY() - 64, player.getBlockY() + 64);
                    int region_min_z = Math.min(player.getBlockZ() - 64, player.getBlockZ() + 64);
                    int region_max_z = Math.max(player.getBlockZ() - 64, player.getBlockZ() + 64);

                    // Region size by X or Z can't be > 450
                    if (region_max_x - region_min_x > 320) {
                        throw new IllegalArgumentException("Region size by X axis can't be > 450");
                    }
                    if (region_max_z - region_min_z > 320) {
                        throw new IllegalArgumentException("Region size by Z axis can't be > 450");
                    }

                    // Render region
                    RenderCubeUtils.renderRegion(
                            player.level(),
                            fileWriters,
                            new int[]{region_min_x, region_min_y, region_min_z, region_max_x, region_max_y, region_max_z});

                    // Notify about success
                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Render succeeded."));
                }
                catch(Exception e) {
                    // Notify about exception
                    Minecraft.getInstance().player.sendSystemMessage(
                            Component.literal(new Throwable().getStackTrace()[0].getMethodName() + ": " + e));
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents{
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
}
