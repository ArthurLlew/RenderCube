package com.render_cube.events;

import com.mojang.logging.LogUtils;
import com.render_cube.RenderCube;
import com.render_cube.utils.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEventsHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Client setup.
     */
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event){
        LOGGER.info("RENDERCUBE CLIENT SETUP");
        LOGGER.info("RENDERCUBE CLIENT SETUP COMPLETE");
    }

    /**
     * Registers custom key bindings.
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(KeyBinding.RENDER_KEY);
        event.register(KeyBinding.DUMP_TEXTURES_KEY);
    }
}
