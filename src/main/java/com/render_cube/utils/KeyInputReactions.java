package com.render_cube.utils;

import com.mojang.logging.LogUtils;
import com.render_cube.RenderCube;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.nio.file.Paths;

import static com.render_cube.utils.CubesRenderer.renderRegion;

public class KeyInputReactions {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void renderCubes(){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null){
            try (FileWriters fileWriters = new FileWriters()){
                // Min and max coordinates over each axes
                int region_min_x = Math.min(player.getBlockX() - 16, player.getBlockX() + 16);
                int region_min_y = Math.min(player.getBlockY() - 16, player.getBlockY() + 16);
                int region_min_z = Math.min(player.getBlockZ() - 16, player.getBlockZ() + 16);
                int region_max_x = Math.max(player.getBlockX() - 16, player.getBlockX() + 16);
                int region_max_y = Math.max(player.getBlockY() - 16, player.getBlockY() + 16);
                int region_max_z = Math.max(player.getBlockZ() - 16, player.getBlockZ() + 16);

                // Restrict region size
                if (region_max_x - region_min_x > 320) {
                    throw new IllegalArgumentException("Region size by X axis can't be > 450");
                }
                if (region_max_z - region_min_z > 320) {
                    throw new IllegalArgumentException("Region size by Z axis can't be > 450");
                }

                // Render region
                renderRegion(player.level(), fileWriters,
                        new BlockPos(region_min_x, region_min_y, region_min_z),
                        new BlockPos(region_max_x, region_max_y, region_max_z));

                // Notify about success
                player.sendSystemMessage(Component.literal("Render succeeded."));

            }
            catch(Exception e) {
                player.sendSystemMessage(Component.literal(
                        new Throwable().getStackTrace()[0].getMethodName() + ": " + e));
            }
        }
        else{
            LOGGER.info("Player is suddenly null");
        }
    }

    public static void dumpTextures(){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null){
            player.sendSystemMessage(Component.literal("Dumping texture atlases"));
            Minecraft.getInstance().getTextureManager().dumpAllSheets(Paths.get(RenderCube.TEXTURE_ATLASES_DIR));
            player.sendSystemMessage(Component.literal("Texture atlases saved successfully"));
        }
        else{
            LOGGER.info("Player is suddenly null");
        }
    }
}
