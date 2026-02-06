package net.arthurllew.rendercube.client;

import net.arthurllew.rendercube.client.keyboard.KeyBindings;
import net.arthurllew.rendercube.client.keyboard.KeyInputReactions;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class RenderCubeClient implements ClientModInitializer {
    /**
     * Client-side initialization.
     */
    @Override
    public void onInitializeClient() {
        // Register keybinding
        KeyBindingHelper.registerKeyBinding(KeyBindings.RENDER_SCREEN_KEY);

        // Register keybinding handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (KeyBindings.RENDER_SCREEN_KEY.consumeClick()) {
                KeyInputReactions.openRenderScreen();
            }
        });
    }
}
