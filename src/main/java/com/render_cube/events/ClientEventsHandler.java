package com.render_cube.events;

import com.render_cube.RenderCube;
import com.render_cube.commands.RenderCubeCommand;
import com.render_cube.commands.RenderRegionCommand;
import com.render_cube.utils.FileWriters;
import com.render_cube.utils.KeyBinding;
import com.render_cube.utils.RenderCubeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = RenderCube.MODID, value = Dist.CLIENT)
public class ClientEventsHandler {
    /**
     * Key pressing logic.
     */
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event){
        if(KeyBinding.RENDER_REGION_KEY.consumeClick()){
            try (FileWriters fileWriters = new FileWriters()){
                LocalPlayer player = Minecraft.getInstance().player;

                // Min and max coordinates over each axes
                int region_min_x = Math.min(player.getBlockX() - 16, player.getBlockX() + 16);
                int region_max_x = Math.max(player.getBlockX() - 16, player.getBlockX() + 16);
                int region_min_y = Math.min(player.getBlockY() - 16, player.getBlockY() + 16);
                int region_max_y = Math.max(player.getBlockY() - 16, player.getBlockY() + 16);
                int region_min_z = Math.min(player.getBlockZ() - 16, player.getBlockZ() + 16);
                int region_max_z = Math.max(player.getBlockZ() - 16, player.getBlockZ() + 16);

                // Restrict region size
                if (region_max_x - region_min_x > 320) {
                    throw new IllegalArgumentException("Region size by X axis can't be > 450");
                }
                if (region_max_z - region_min_z > 320) {
                    throw new IllegalArgumentException("Region size by Z axis can't be > 450");
                }

                // Render region
                RenderCubeUtils.renderRegion(
                        player.level(),
                        fileWriters,
                        new int[]{region_min_x, region_min_y, region_min_z, region_max_x, region_max_y, region_max_z});

                // Notify about success
                Minecraft.getInstance().player.sendSystemMessage(Component.literal("Render succeeded."));
            }
            catch(Exception e) {
                // Notify about exception
                Minecraft.getInstance().player.sendSystemMessage(
                        Component.literal(new Throwable().getStackTrace()[0].getMethodName() + ": " + e));
            }
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
