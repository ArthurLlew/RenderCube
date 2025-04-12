package com.rendercube.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import static com.rendercube.RenderCube.MODID;

public abstract class KeyBindings {
    public static final String MOD_KEY_CATEGORY = "key.category." + MODID + ".rendercube";

    public static final KeyMapping RENDER_SCREEN_KEY = new KeyMapping("key." + MODID + ".render_screen",
                                                                      KeyConflictContext.IN_GAME,
                                                                      InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R,
                                                                      MOD_KEY_CATEGORY);
}
