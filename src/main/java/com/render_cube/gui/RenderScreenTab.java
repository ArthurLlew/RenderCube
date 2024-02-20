package com.render_cube.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderScreenTab extends AbstractWidget {
    /**
     * Function, executed when widget is clicked.
     */
    protected final OnClick onClick;
    /**
     * Used textures.
     */
    protected final ResourceLocation selectedTexture, unselectedTexture;
    /**
     * Item being displayed over tab.
     */
    protected final ItemStack itemstack;
    /**
     * Currently rendered texture.
     */
    protected ResourceLocation currentTexture;
    /**
     * Tab type.
     */
    public final RenderScreenTabType type;
    /**
     * Renders tab contents.
     */
    public final RenderMethod renderMethod;
    /**
     * Tab textures.
     */

    RenderScreenTab(int posLeft, int posTop, ResourceLocation selectedTexture, ResourceLocation unselectedTexture,
                    Component title, RenderMethod renderMethod, OnClick onClick, ItemStack itemstack,
                    RenderScreenTabType type){
        super(posLeft, posTop, 26, 32, CommonComponents.EMPTY);
        this.selectedTexture = selectedTexture;
        this.unselectedTexture = unselectedTexture;
        this.itemstack = itemstack;
        this.renderMethod = renderMethod;
        this.onClick = onClick;
        this.type = type;

        this.setTooltip(Tooltip.create(title));

        this.setUnselected();
    }

    /**
     * Renders tab button.
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        // Tab button texture
        guiGraphics.blit(currentTexture, this.getX(), this.getY(),0, 0,
                width, height, width, height);

        // Render on top of texture item
        int posX = this.getX() + 5;
        int popY = this.getY() + 8;
        guiGraphics.renderItem(itemstack, posX, popY);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemstack, posX, popY);
    }

    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    /**
     * Changes texture to selected version.
     */
    public void setSelected(){
        currentTexture = selectedTexture;
    }

    /**
     * Changes texture to unselected version.
     */
    public void setUnselected(){
        currentTexture = unselectedTexture;
    }

    @Override
    public void onClick(double pMouseX, double pMouseY) {
        this.onClick.onClick(this);
    }

    @OnlyIn(Dist.CLIENT)
    public interface RenderMethod {
        void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnClick {
        void onClick(RenderScreenTab tab);
    }
}
