package com.rendercube.client;

import com.mojang.logging.LogUtils;
import com.rendercube.client.gui.RenderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;

public class KeyInputReactions {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void openRenderScreen() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().setScreen(new RenderScreen());
        }
        else {
            LOGGER.error("Attempt to open render screen while player is NULL");
        }
    }
}
