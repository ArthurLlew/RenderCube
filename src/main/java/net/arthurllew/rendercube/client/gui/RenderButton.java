package net.arthurllew.rendercube.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
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

    @Environment(EnvType.CLIENT)
    public interface OnPress {
        void onPress(RenderButton button);
    }
}
