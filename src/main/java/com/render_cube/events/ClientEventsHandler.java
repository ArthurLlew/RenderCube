package com.render_cube.events;

import com.render_cube.RenderCube;
import com.render_cube.commands.RenderCubeCommand;
import com.render_cube.commands.RenderRegionCommand;
import com.render_cube.utils.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

import static com.render_cube.utils.KeyInputReactions.dumpTextures;
import static com.render_cube.utils.KeyInputReactions.renderCubes;

@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
public class ClientEventsHandler {
    /**
     * Key pressing logic.
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event){
        if(KeyBinding.RENDER_KEY.consumeClick()){
            renderCubes();
        }
        else if(KeyBinding.DUMP_TEXTURES_KEY.consumeClick()){
            dumpTextures();
        }
    }

    /**
     * Registers custom commands.
     * @param event command registering event
     */
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        // Render block command
        new RenderCubeCommand(event.getDispatcher());

        // Render region command
        new RenderRegionCommand(event.getDispatcher());

        // ?
        ConfigCommand.register(event.getDispatcher());
    }
}
