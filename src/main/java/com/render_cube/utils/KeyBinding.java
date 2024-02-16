package com.render_cube.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBinding {
    public static final String KEY_CATEGORY = "key.category.rendercube.rendercube";
    public static final String KEY_RENDER = "key.rendercube.render";
    public static final String KEY_DUMP_TEXTURES = "key.rendercube.dump_textures";

    public static final KeyMapping RENDER_KEY = new KeyMapping(KEY_RENDER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);
    public static final KeyMapping DUMP_TEXTURES_KEY = new KeyMapping(KEY_DUMP_TEXTURES, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, KEY_CATEGORY);
}
