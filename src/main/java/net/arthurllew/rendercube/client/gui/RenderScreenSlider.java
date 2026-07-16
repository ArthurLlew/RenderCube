package net.arthurllew.rendercube.client.gui;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

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
     * Constructor.
     * @param x            X widget coordinate
     * @param y            Y widget coordinate
     * @param width        widget width
     * @param height       widget width
     * @param initialValue slider starting value
     */
    public RenderScreenSlider(int x, int y, int width, int height, double maxValue, int initialValue) {
        super(x, y, width, height, Component.literal(""), initialValue / maxValue);
        this.maxValue = maxValue;
        this.intValue = initialValue;
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
     * Updates slider text.
     */
    @Override
    protected void updateMessage() {
        setMessage(Component.literal("LOD: " + intValue));
    }

    /**
     * Updates slider value.
     */
    @Override
    protected void applyValue() {
        // Snap value to the nearest integer
        this.intValue = (int) Math.round(value * maxValue);
        // Derive position from int value
        this.value = intValue / maxValue;
    }
}
