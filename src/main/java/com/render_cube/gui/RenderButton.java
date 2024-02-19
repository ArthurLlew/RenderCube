package com.render_cube.gui;

import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class RenderButton extends AbstractButton {
    protected final RenderButton.OnPress onPress;
    protected final RenderButton.CreateNarration createNarration;

    public RenderButton(int posX, int posY, int width, int height, Component message,
                        RenderButton.OnPress onPress,
                        RenderButton.CreateNarration createNarration) {
        super(posX, posY, width, height, message);
        this.onPress = onPress;
        this.createNarration = createNarration;
    }

    public RenderButton(int posX, int posY, int width, int height, Component message,
                        RenderButton.OnPress onPress) {
        this(posX, posY, width, height, message, onPress, Supplier::get);
    }

    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    public void onRelease(double pMouseX, double pMouseY) {
        // Upon release the button should stop being highlighted
        setFocused(false);
    }

    protected @NotNull MutableComponent createNarrationMessage() {
        return this.createNarration.createNarrationMessage(super::createNarrationMessage);
    }

    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        MutableComponent createNarrationMessage(Supplier<MutableComponent> messageSupplier);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(RenderButton button);
    }
}
