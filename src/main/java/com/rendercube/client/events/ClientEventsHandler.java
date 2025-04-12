package com.rendercube.client.events;

import com.rendercube.RenderCube;
import com.rendercube.client.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.rendercube.client.KeyInputReactions.openRenderScreen;

@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
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
