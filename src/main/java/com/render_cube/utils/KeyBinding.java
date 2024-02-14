package com.render_cube.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY = "key.category.rendercube.rendercube";
    public static final String KEY_RENDER_REGION = "key.rendercube.render_region";

    public static final KeyMapping RENDER_REGION_KEY = new KeyMapping(KEY_RENDER_REGION, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);
}
