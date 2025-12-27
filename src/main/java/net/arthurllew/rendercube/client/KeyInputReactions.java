package net.arthurllew.rendercube.client;

import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.gui.RenderScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class KeyInputReactions {
    public static void openRenderScreen() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().setScreen(new RenderScreen());
        }
        else {
            RenderCube.LOGGER.error("Attempt to open render screen while player is NULL");
        }
    }
}
