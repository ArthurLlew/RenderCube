package net.arthurllew.rendercube;

import net.arthurllew.rendercube.key_bindings.KeyBindings;
import net.arthurllew.rendercube.key_bindings.KeyInputReactions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class RenderCubeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register custom keybindings
        KeyBindingHelper.registerKeyBinding(KeyBindings.INSTANCE.RENDER_SCREEN_KEY);
        KeyBindingHelper.registerKeyBinding(KeyBindings.INSTANCE.DUMP_TEXTURES_KEY);

        // Register reaction for pressing a custom key
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.INSTANCE.RENDER_SCREEN_KEY.wasPressed()) {
                KeyInputReactions.openRenderScreen();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.INSTANCE.DUMP_TEXTURES_KEY.wasPressed()) {
                KeyInputReactions.dumpTextures();
            }
        });
    }
}
