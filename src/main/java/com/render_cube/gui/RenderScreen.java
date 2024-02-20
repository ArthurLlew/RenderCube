package com.render_cube.gui;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

import static com.render_cube.RenderCube.MODID;
import static com.render_cube.rendering.CubesRenderer.renderRegion;

// TODO: edit-boxes for position input
@OnlyIn(Dist.CLIENT)
public class RenderScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation(MODID, "textures/gui/render_screen.png");
    private static final ResourceLocation[] TAB_TEXTURES =
            new ResourceLocation[]{
                    new ResourceLocation(MODID, "textures/gui/tab_top_right_selected.png"),
                    new ResourceLocation(MODID, "textures/gui/tab_top_right_unselected.png"),
                    new ResourceLocation(MODID, "textures/gui/tab_top_middle_selected.png"),
                    new ResourceLocation(MODID, "textures/gui/tab_top_middle_unselected.png")};
    private static final Component[] TAB_TITLES = new Component[]{
            Component.translatable("gui." + MODID + ".render_screen.prr_tab"),
            Component.translatable("gui." + MODID + ".render_screen.apr_tab")};
    private static final Component RENDER_BUTTON_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.button.render");
    private static final Component RENDER_SUCCESS_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.success");
    private static final Component RENDER_ERROR_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.error");

    /**
     * Background texture dimensions.
     */
    private final int bgWidth, bgHeight;

    /**
     * Background texture positions.
     */
    private int bgPosLeft, bgPosTop;

    /**
     * Tabs, that should not display their contents and render their buttons below the background.
     */
    private final List<RenderScreenTab> deselectedTabs = Lists.newArrayList();

    /**
     * Active tab.
     */
    private static RenderScreenTab selectedTab;

    /**
     * Holds render button instance.
     */
    private RenderButton renderButton;

    public RenderScreen() {
        super(CommonComponents.EMPTY);

        this.bgWidth = 195;
        this.bgHeight = 136;
    }

    /**
     * Tells, if game should be paused.
     * @return {@code false}
     */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Initializes GUI element.
     */
    @Override
    protected void init(){
        super.init();

        // Set background texture coordinates in screen center
        bgPosLeft = (this.width - bgWidth) / 2;
        bgPosTop = (this.height - bgHeight) / 2;

        // Populate tabs
        deselectedTabs.add(addWidget(new RenderScreenTab(bgPosLeft, bgPosTop - 28,
                TAB_TEXTURES[0], TAB_TEXTURES[1], TAB_TITLES[0],
                this::renderPRR, this::onTabPressed, new ItemStack(Items.PLAYER_HEAD),
                RenderScreenTabType.PLAYER_RELATIVE_RENDER)));
        deselectedTabs.add(addWidget(new RenderScreenTab(bgPosLeft + 27, bgPosTop - 28,
                TAB_TEXTURES[2], TAB_TEXTURES[3], TAB_TITLES[1],
                this::renderAPR, this::onTabPressed, new ItemStack(Items.GRASS_BLOCK),
                RenderScreenTabType.ABSOLUTE_POSITION_RENDER)));

        // If no tab was selected, select the first one
        if (selectedTab == null){
            selectedTab = deselectedTabs.get(0);
            selectedTab.setSelected();
        }

        // Render button
        renderButton = addWidget(new RenderButton(bgPosLeft + bgWidth / 2 - 30,
                bgPosTop + bgHeight - 26,
                60,
                20,
                RENDER_BUTTON_TEXT,
                this::onRenderButtonPressed)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Render all unselected tabs under background texture
        for(RenderScreenTab tab : this.deselectedTabs){
            tab.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // Background texture
        guiGraphics.blit(BACKGROUND_TEXTURE, bgPosLeft, bgPosTop,0, 0,
                bgWidth, bgHeight, bgWidth, bgHeight);

        // Render selected tab
        selectedTab.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

        // Render current tab contents
        selectedTab.renderMethod.render(guiGraphics, mouseX, mouseY, partialTicks);
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
        guiGraphics.drawString(this.font, TAB_TITLES[0], bgPosLeft + 8, bgPosTop + 6,
                0x404040, false);

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
        guiGraphics.drawString(this.font, TAB_TITLES[1], bgPosLeft + 8, bgPosTop + 6,
                0x404040, false);

        renderButton.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Handles tab being pressed.
     * @param tab pressed tab instance
     */
    private void onTabPressed(RenderScreenTab tab){
        if (tab != selectedTab){
            // Deselect current tab
            selectedTab.setUnselected();
            // Remove selected tab from unselected list
            deselectedTabs.remove(tab);
            // Add deselected tab to unselected list
            deselectedTabs.add(selectedTab);
            // Assign new selected tab
            selectedTab = tab;
            // Set its state as active
            selectedTab.setSelected();
        }
    }

    /**
     * Handles render button being pressed.
     * @param button pressed button instance
     */
    private void onRenderButtonPressed(AbstractButton button){
        // Safely get minecraft player
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {throw new UnsupportedOperationException("Player is null");}

        try (FileWriters fileWriters = new FileWriters()){
            // TODO: bound this to edit-boxes
            int region_x1 = Math.min(player.getBlockX() - 16, player.getBlockX() + 16);
            int region_y1 = Math.min(player.getBlockY() - 16, player.getBlockY() + 16);
            int region_z1 = Math.min(player.getBlockZ() - 16, player.getBlockZ() + 16);
            int region_x2 = Math.max(player.getBlockX() - 16, player.getBlockX() + 16);
            int region_y2 = Math.max(player.getBlockY() - 16, player.getBlockY() + 16);
            int region_z2 = Math.max(player.getBlockZ() - 16, player.getBlockZ() + 16);

            // Min/max positions in region
            BlockPos posMin, posMax;

            if (selectedTab.type == RenderScreenTabType.PLAYER_RELATIVE_RENDER){
                // We do not need to check positions
                posMin = new BlockPos(region_x1, region_y1, region_z1);
                posMax = new BlockPos(region_x2, region_y2, region_z2);
            }
            else{
                // Place min x/y/z into minPos and max x/y/z into maxPos
                int regionMinX = Math.min(region_x1, region_x2);
                int regionMinY = Math.min(region_y1, region_y2);
                int regionMinZ = Math.min(region_z1, region_z2);
                int regionMaxX = Math.max(region_x1, region_x2);
                int regionMaxY = Math.max(region_y1, region_y2);
                int regionMaxZ = Math.max(region_z1, region_z2);

                posMin = new BlockPos(regionMinX, regionMinY, regionMinZ);
                posMax = new BlockPos(regionMaxX, regionMaxY, regionMaxZ);

            }

            // Restrict region size
            if (posMax.getX() - posMin.getX() > 320) {
                throw new IllegalArgumentException("Region size by X axis can't be > 450");
            }
            if (posMax.getZ() - posMin.getZ() > 320) {
                throw new IllegalArgumentException("Region size by Z axis can't be > 450");
            }

            // Render region
            renderRegion(player.level(), fileWriters, posMin, posMax);

            // Notify about success
            player.sendSystemMessage(RENDER_SUCCESS_MSG);

        }
        catch(Exception e) {
            LOGGER.error("RenderCube encountered error while rendering", e);
            player.sendSystemMessage(RENDER_ERROR_MSG);
        }
    }
}
