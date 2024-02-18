package com.render_cube.events;

import com.render_cube.RenderCube;
import com.render_cube.key_bindings.KeyBindings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.render_cube.key_bindings.KeyInputReactions.dumpTextures;
import static com.render_cube.key_bindings.KeyInputReactions.openRenderScreen;

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
