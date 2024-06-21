package com.rendercube.key_bindings;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static com.rendercube.RenderCube.MODID;

public final class KeyBindings {
    public static final KeyBindings INSTANCE = new KeyBindings();

    private KeyBindings() {}

    public static final String MOD_KEY_CATEGORY = "key.category." + MODID + ".rendercube";

    public final KeyBinding RENDER_SCREEN_KEY = new KeyBinding("key." +MODID + ".render_screen",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, MOD_KEY_CATEGORY);
    public final KeyBinding DUMP_TEXTURES_KEY = new KeyBinding("key." + MODID + ".dump_textures",
            InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, MOD_KEY_CATEGORY);
}
