package net.arthurllew.rendercube.client.events;

import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.keyboard.KeyBindings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

import static net.arthurllew.rendercube.client.keyboard.KeyInputReactions.openRenderScreen;

@EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
public class ClientEventsHandler {
    /**
     * Key pressed event.
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event){
        if(KeyBindings.RENDER_SCREEN_KEY.consumeClick()){
            openRenderScreen();
        }
    }
}
