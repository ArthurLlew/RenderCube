package net.arthurllew.rendercube.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

@Environment(value= EnvType.CLIENT)
public class RenderButton extends PressableWidget {
    protected final RenderButton.OnPress onPress;

    public RenderButton(int posX, int posY, int width, int height, Text message,
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

    public void appendClickableNarrations(@NotNull NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    @Environment(value= EnvType.CLIENT)
    public interface OnPress {
        void onPress(RenderButton button);
    }
}
