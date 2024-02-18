package com.render_cube.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ReleasableVanillaButton extends Button {

    protected ReleasableVanillaButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, Button.OnPress pOnPress, Button.CreateNarration pCreateNarration){
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, pCreateNarration);
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        // Upon release the button should stop being highlighted
        setFocused(false);
    }
}
