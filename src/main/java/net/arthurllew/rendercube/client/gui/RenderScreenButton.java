package net.arthurllew.rendercube.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class RenderScreenButton extends AbstractButton {
    /**
     * Button being clicked handler.
     */
    protected final OnClick onClick;

    /**
     * Constructor.
     * @param x       X widget coordinate
     * @param y       Y widget coordinate
     * @param width   widget width
     * @param height  widget width
     * @param text    button text
     * @param onClick button being clicked handler
     */
    public RenderScreenButton(int x, int y, int width, int height, Component text, OnClick onClick) {
        super(x, y, width, height, text);
        this.onClick = onClick;
    }

    @Override
    public void onPress() {
        this.onClick.handle();
    }

    /**
     * Button being released handler.
     */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        // Upon release the button should stop being highlighted
        setFocused(false);
    }

    /**
     * Sets button narration.
     */
    @Override
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    /**
     * Button being clicked handler.
     */
    @Environment(EnvType.CLIENT)
    public interface OnClick {
        void handle();
    }
}
