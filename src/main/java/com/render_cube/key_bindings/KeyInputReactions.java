package com.render_cube.key_bindings;

import com.mojang.logging.LogUtils;
import com.render_cube.RenderCube;
import com.render_cube.gui.RenderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.nio.file.Paths;

public class KeyInputReactions {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void openRenderScreen(){
        Minecraft.getInstance().setScreen(new RenderScreen());
    }

    public static void dumpTextures(){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null){
            Minecraft.getInstance().getTextureManager().dumpAllSheets(Paths.get(RenderCube.TEXTURE_ATLASES_DIR));
            player.sendSystemMessage(Component.literal("Texture atlases saved successfully"));
        }
        else{
            LOGGER.info("Player is suddenly null");
        }
    }
}
