package net.arthurllew.rendercube.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

@Environment(value= EnvType.CLIENT)
public class RenderScreenTab extends ClickableWidget {
    /**
     * Function, executed when widget is clicked.
     */
    protected final OnClick onClick;
    /**
     * Used textures.
     */
    protected final Identifier selectedTexture, unselectedTexture;
    /**
     * Item being displayed over tab.
     */
    protected final ItemStack itemIcon;
    /**
     * Currently rendered texture.
     */
    protected Identifier currentTexture;
    /**
     * Tab type.
     */
    public final RenderScreenTab.Type type;
    /**
     * Renders tab contents.
     */
    public final RenderMethod renderMethod;
    /**
     * Tab textures.
     */

    RenderScreenTab(int posLeft, int posTop, Identifier selectedTexture, Identifier unselectedTexture,
                    Text title, RenderMethod renderMethod, OnClick onClick, ItemStack itemIcon,
                    RenderScreenTab.Type type){
        super(posLeft, posTop, 26, 32, title);
        this.selectedTexture = selectedTexture;
        this.unselectedTexture = unselectedTexture;
        this.itemIcon = itemIcon;
        this.renderMethod = renderMethod;
        this.onClick = onClick;
        this.type = type;

        setTooltip(Tooltip.of(title));

        this.setUnselected();
    }

    /**
     * Renders tab button.
     * @param drawContext the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderButton(@NotNull DrawContext drawContext, int mouseX, int mouseY, float partialTicks){
        // Tab button texture
        drawContext.drawTexture(currentTexture, this.getX(), this.getY(),0, 0,
                width, height, width, height);

        // Render on top of texture item
        int posX = this.getX() + 5;
        int popY = this.getY() + 8;
        drawContext.drawItem(itemIcon, posX, popY);
        drawContext.drawItemInSlot(MinecraftClient.getInstance().textRenderer, itemIcon, posX, popY);
    }

    public void appendClickableNarrations(@NotNull NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }

    /**
     * Defines position of tooltip.
     * @return {@link HoveredTooltipPositioner}
     */
    protected @NotNull TooltipPositioner getTooltipPositioner() { return HoveredTooltipPositioner.INSTANCE; }

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

    /**
     * Defines reaction after being clicked by mouse.
     * @param pMouseX the x-coordinate of the mouse cursor.
     * @param pMouseY the y-coordinate of the mouse cursor.
     */
    @Override
    public void onClick(double pMouseX, double pMouseY) {
        this.onClick.onClick(this);
    }

    /**
     * Tab types.
     */
    public enum Type {
        PLAYER_RELATIVE_RENDER,
        ABSOLUTE_POSITION_RENDER
    }

    @Environment(value= EnvType.CLIENT)
    public interface RenderMethod {
        void render(@NotNull DrawContext drawContext, int mouseX, int mouseY, float partialTicks);
    }

    @Environment(value= EnvType.CLIENT)
    public interface OnClick {
        void onClick(RenderScreenTab tab);
    }
}
