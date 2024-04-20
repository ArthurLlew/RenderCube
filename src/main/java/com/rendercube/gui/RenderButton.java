package com.rendercube.gui;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderButton extends AbstractButton {
    protected final RenderButton.OnPress onPress;

    public RenderButton(int posX, int posY, int width, int height, Component message,
                        RenderButton.OnPress onPress) {
        super(posX, posY, width, height, message);
        this.onPress = onPress;
    }

    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        // Upon release the button should stop being highlighted
        setFocused(false);
    }

    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(RenderButton button);
    }
}
