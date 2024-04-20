package com.rendercube.events;

import com.mojang.logging.LogUtils;
import com.rendercube.RenderCube;
import com.rendercube.key_bindings.KeyBindings;
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
        LOGGER.info("RenderCube Client setup");
        LOGGER.info("RenderCube Client setup complete");
    }

    /**
     * Registers custom key bindings.
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(KeyBindings.INSTANCE.RENDER_SCREEN_KEY);
        event.register(KeyBindings.INSTANCE.DUMP_TEXTURES_KEY);
    }
}
