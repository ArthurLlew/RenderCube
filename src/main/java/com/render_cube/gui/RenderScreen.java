package com.render_cube.gui;

import com.google.common.collect.Lists;
import com.render_cube.rendering.FileWriters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.render_cube.RenderCube.MODID;
import static com.render_cube.rendering.CubesRenderer.renderRegion;

// TODO: edit-boxes for position input
@OnlyIn(Dist.CLIENT)
public class RenderScreen extends Screen {
    /**
     * Player relative render tab title.
     */
    private static final Component PRR_TAB = Component.translatable("gui." + MODID + ".render_screen.prr_tab");
    /**
     * Absolute position render tab title.
     */
    private static final Component APR_TAB = Component.translatable("gui." + MODID + ".render_screen.apr_tab");
    /**
     * Render button text.
     */
    private static final Component RENDER_BUTTON = Component.translatable("gui." + MODID + ".render_screen.button.render");
    private static final ResourceLocation RENDER_SCREEN_TEXTURE = new ResourceLocation(MODID, "textures/gui/render_screen.png");

    /**
     * Background texture dimensions.
     */
    private final int bgWidth, bgHeight;

    /**
     * Background texture positions.
     */
    private int bgPosLeft, bgPosTop;

    private final List<RenderScreenTab> tabs = Lists.newArrayList();

    // TODO: load this from forge config
    private RenderScreenTab currentTab;

    private RenderButton renderButton;

    public RenderScreen() {
        super(CommonComponents.EMPTY);

        this.bgWidth = 195;
        this.bgHeight = 136;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init(){
        super.init();

        // Set background texture coordinates in screen center
        bgPosLeft = (this.width - bgWidth) / 2;
        bgPosTop = (this.height - bgHeight) / 2;

        // Populate tabs
        tabs.add(new RenderScreenTab(RenderScreenTabType.PLAYER_RELATIVE_RENDER,
                bgPosLeft,
                bgPosTop - 28,
                RENDER_SCREEN_TEXTURE,
                this::renderPRR));
        tabs.add(new RenderScreenTab(RenderScreenTabType.ABSOLUTE_POSITION_RENDER,
                bgPosLeft + 27,
                bgPosTop - 28,
                RENDER_SCREEN_TEXTURE,
                this::renderAPR));
        // Select current tab // TODO: remove this line
        currentTab = tabs.get(0);

        // Render button
        renderButton = addWidget(new RenderButton(bgPosLeft + bgWidth / 2 - 30,
                bgPosTop + bgHeight - 26,
                60,
                20,
                RENDER_BUTTON,
                this::handleRenderButton)
        );
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Background texture
        guiGraphics.blit(RENDER_SCREEN_TEXTURE, bgPosLeft, bgPosTop, 0, 0, bgWidth, bgHeight);
        // Tab 1
        guiGraphics.blit(RENDER_SCREEN_TEXTURE, bgPosLeft, bgPosTop - 28, 0, 136, 26, 32);
        // Tab 2
        guiGraphics.blit(RENDER_SCREEN_TEXTURE, bgPosLeft + 27, bgPosTop - 28, 26, 136, 26, 32);

        // Render current tab contents
        currentTab.renderMethod.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders player relative render tab.
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderPRR(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        // Tab title
        guiGraphics.drawString(this.font, PRR_TAB, bgPosLeft + 8, bgPosTop + 6, 0x404040, false);

        renderButton.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders absolute position render tab.
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderAPR(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        // Tab title
        guiGraphics.drawString(this.font, APR_TAB, bgPosLeft + 8, bgPosTop + 6, 0x404040, false);

        renderButton.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Do only if button will not be consumed anyway
        if (button == 0) {

            // Check every tab for being clicked
            for(RenderScreenTab tab : tabs){
                if (tab.checkClicked(mouseX, mouseY)){
                    currentTab = tab;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleRenderButton(AbstractButton button){
        // TODO: edit-box input being processed differently in different tabs

        // Safely get minecraft player
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {throw new UnsupportedOperationException("Player is null");}

        try (FileWriters fileWriters = new FileWriters()){
            // Min and max coordinates over each axes
            int region_min_x = Math.min(player.getBlockX() - 16, player.getBlockX() + 16);
            int region_min_y = Math.min(player.getBlockY() - 16, player.getBlockY() + 16);
            int region_min_z = Math.min(player.getBlockZ() - 16, player.getBlockZ() + 16);
            int region_max_x = Math.max(player.getBlockX() - 16, player.getBlockX() + 16);
            int region_max_y = Math.max(player.getBlockY() - 16, player.getBlockY() + 16);
            int region_max_z = Math.max(player.getBlockZ() - 16, player.getBlockZ() + 16);

            // Restrict region size
            if (region_max_x - region_min_x > 320) {
                throw new IllegalArgumentException("Region size by X axis can't be > 450");
            }
            if (region_max_z - region_min_z > 320) {
                throw new IllegalArgumentException("Region size by Z axis can't be > 450");
            }

            // Render region
            renderRegion(player.level(), fileWriters,
                    new BlockPos(region_min_x, region_min_y, region_min_z),
                    new BlockPos(region_max_x, region_max_y, region_max_z));

            // Notify about success
            player.sendSystemMessage(Component.literal("Render succeeded"));

        }
        catch(Exception e) {
            // TODO: log errors
            player.sendSystemMessage(Component.literal(
                    new Throwable().getStackTrace()[0].getMethodName() + ": " + e));
        }
    }
}
