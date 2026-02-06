package net.arthurllew.rendercube.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

import static net.arthurllew.rendercube.RenderCube.MODID;

public abstract class KeyBindings {
    public static final String MOD_KEY_CATEGORY = "key.category." + MODID + ".rendercube";

    public static final KeyMapping RENDER_SCREEN_KEY = new KeyMapping("key." + MODID + ".render_screen",
                                                                      InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R,
                                                                      MOD_KEY_CATEGORY);
}
