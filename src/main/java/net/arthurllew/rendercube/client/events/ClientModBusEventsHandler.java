package net.arthurllew.rendercube.client.events;

import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.keyboard.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Mod client setup.
 */
@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModBusEventsHandler {
    /**
     * Registers custom key bindings.
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(KeyBindings.RENDER_SCREEN_KEY);
    }
}
