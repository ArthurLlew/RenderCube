package com.rendercube.events;

import com.rendercube.RenderCube;
import com.rendercube.key_bindings.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.rendercube.key_bindings.KeyInputReactions.dumpTextures;
import static com.rendercube.key_bindings.KeyInputReactions.openRenderScreen;

@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
public class ClientEventsHandler {
    /**
     * Key pressing logic.
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event){
        if(KeyBindings.INSTANCE.RENDER_SCREEN_KEY.consumeClick()){
            openRenderScreen();
        }
        else if(KeyBindings.INSTANCE.DUMP_TEXTURES_KEY.consumeClick()){
            dumpTextures();
        }
    }
}
