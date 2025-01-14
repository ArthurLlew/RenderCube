package com.rendercube.key_bindings;

import com.mojang.logging.LogUtils;
import com.rendercube.RenderCube;
import com.rendercube.gui.RenderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.nio.file.Paths;

public class KeyInputReactions {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void openRenderScreen(){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().setScreen(new RenderScreen());
        }
        else{
            LOGGER.error("Attempt to open render screen while player is NULL");
        }
    }

    public static void dumpTextures(){
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null){
            Minecraft.getInstance().getTextureManager().dumpAllSheets(Paths.get(RenderCube.TEXTURE_ATLASES_DIR));
            player.sendSystemMessage(Component.literal("Texture atlases saved successfully"));
        }
        else{
            LOGGER.error("Attempt to dump textures while player is NULL");
        }
    }
}
