package net.arthurllew.rendercube.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderScreenSlider extends AbstractSliderButton {
    /**
     * Slider max (converted) integer value.
     */
    protected final double maxValue;

    /**
     * Slider integer value.
     */
    protected int intValue;

    /**
     * Slider value change handler.
     */
    protected final OnValueChange onValueChange;

    /**
     * Constructor.
     * @param x             X slider coordinate
     * @param y             Y slider coordinate
     * @param width         slider width
     * @param height        slider height
     * @param maxValue      slider maximum value
     * @param initialValue  slider starting value
     * @param onValueChange slider value change handler
     */
    public RenderScreenSlider(int x, int y, int width, int height, double maxValue, int initialValue,
                              OnValueChange onValueChange) {
        super(x, y, width, height, Component.literal(""), initialValue / maxValue);
        this.maxValue = maxValue;
        this.intValue = initialValue;
        this.onValueChange = onValueChange;
        // Set initial text
        updateMessage();
    }

    /**
     * @return slider integer value.
     */
    public int getValue() {
        return this.intValue;
    }

    /**
     * Is called upon slider value update.
     */
    @Override
    protected void updateMessage() {
        // Update count in text
        setMessage(Component.literal("LOD: " + this.intValue));
        // Handle slider update
        this.onValueChange.handle(this.intValue);
    }

    /**
     * Updates slider value.
     */
    @Override
    protected void applyValue() {
        // Snap value to the nearest integer
        this.intValue = (int) Math.round(this.value * this.maxValue);
        // Derive position from int value
        this.value = this.intValue / this.maxValue;
    }

    /**
     * Slider value change handler.
     */
    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        void handle(int value);
    }
}
