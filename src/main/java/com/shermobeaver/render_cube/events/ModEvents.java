package com.shermobeaver.render_cube.events;

import com.shermobeaver.render_cube.RenderCube;
import com.shermobeaver.render_cube.commands.RenderBlockCommand;
import com.shermobeaver.render_cube.commands.RenderRegionCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = RenderCube.MODID)
public class ModEvents {

    /**
     * Registers custom commands
     * @param event command registering event
     */
    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event){
        // Render block command
        new RenderBlockCommand(event.getDispatcher());

        // Render region command
        new RenderRegionCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
