package net.arthurllew.rendercube.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public abstract class RenderScreenTab extends AbstractWidget {
    /**
     * Tab textures.
     */
    protected final ResourceLocation selectedTexture, deselectedTexture;
    /**
     * Current tab texture.
     */
    protected ResourceLocation currentTexture;
    /**
     * Item being displayed over tab.
     */
    protected final ItemStack itemIcon;

    /**
     * Action, performed when tab is clicked.
     */
    protected final OnClick onClick;

    /**
     * Constructor.
     * @param posX              X position on screen
     * @param posY              Y position on screen
     * @param tooltipText       tab tooltip text
     * @param selectedTexture   tab texture when active
     * @param deselectedTexture tab texture when non-active
     * @param itemIcon          tab item icon
     * @param onClick           action, performed when tab is clicked
     */
    RenderScreenTab(int posX, int posY, Component tooltipText,
                    ResourceLocation selectedTexture, ResourceLocation deselectedTexture, ItemStack itemIcon,
                    OnClick onClick) {
        super(posX, posY, 26, 32, tooltipText);
        // Tab textures
        this.selectedTexture = selectedTexture;
        this.deselectedTexture = deselectedTexture;
        this.deselect(); // not active when created

        // Tab item
        this.itemIcon = itemIcon;

        // Action, performed when tab is clicked
        this.onClick = onClick;

        // Setup tooltip
        setTooltip(Tooltip.create(tooltipText));
    }

    /**
     * Renders tab.
     * @param guiGraphics  GUI renderer
     * @param mouseX       X coordinate of the mouse cursor
     * @param mouseY       Y coordinate of the mouse cursor
     * @param partialTicks partial tick time
     */
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render tab texture
        guiGraphics.blit(this.currentTexture, this.getX(), this.getY(),0, 0,
                this.width, this.height, this.width, this.height);

        // Render tab item
        int posX = this.getX() + 5;
        int popY = this.getY() + 8;
        guiGraphics.renderItem(this.itemIcon, posX, popY);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, this.itemIcon, posX, popY);

        // If tab is selected
        if (this.isSelected()) {
            // Render tab content
            this.renderTabContents(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Renders contents.
     * @param guiGraphics  GUI renderer
     * @param mouseX       X coordinate of the mouse cursor
     * @param mouseY       Y coordinate of the mouse cursor
     * @param partialTicks partial tick time
     */
    public abstract void renderTabContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY,
                                           float partialTicks);

    /**
     * Defines how to render button tooltip.
     */
    public void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    /**
     * @return whether tab is selected
     */
    public boolean isSelected(){
        return this.currentTexture == this.selectedTexture;
    }

    /**
     * Changes texture to selected version.
     */
    public void select(){
        this.currentTexture = this.selectedTexture;
    }

    /**
     * Changes texture to deselected version.
     */
    public void deselect(){
        this.currentTexture = this.deselectedTexture;
    }

    /**
     * Defines reaction for being clicked by mouse.
     * @param mouseX X coordinate of the mouse cursor
     * @param mouseY Y coordinate of the mouse cursor
     */
    @Override
    public void onClick(double mouseX, double mouseY) {
        this.onClick.onClick(this);
    }

    /**
     * Interface for the action, performed when tab is clicked.
     */
    @OnlyIn(Dist.CLIENT)
    public interface OnClick {
        void onClick(RenderScreenTab tab);
    }
}
