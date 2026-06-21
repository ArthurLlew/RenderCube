package net.arthurllew.rendercube.client;

import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.keyboard.KeyBindings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
public class RenderCubeClient {
    /**
     * Registers custom key bindings.
     */
    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event){
        event.register(KeyBindings.RENDER_SCREEN_KEY);
    }
}
