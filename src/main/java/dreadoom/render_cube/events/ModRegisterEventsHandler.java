package dreadoom.render_cube.events;

import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.commands.RenderBlockCommand;
import dreadoom.render_cube.commands.RenderRegionCommand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

/**
 * Defines listeners for register events.
 */
@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
public class ModRegisterEventsHandler {
    /**
     * Registers custom commands.
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
