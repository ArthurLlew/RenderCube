package com.render_cube.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import com.render_cube.rendering.FileWriters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
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

@OnlyIn(Dist.CLIENT)
public class RenderScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Resources
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
    private static final Component[] EDITBOX_TITLES = new Component[]{
            Component.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos1"),
            Component.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos2"),
            Component.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos1"),
            Component.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos2")};
    private static final Component EDITBOX_TOOLTIP =
            Component.translatable("gui." + MODID + ".render_screen.edit_box.tooltip");
    private static final Component RENDER_BUTTON_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.button.render");
    private static final Component RENDER_WRONG_INPUT_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.wrong_input");
    private static final Component RENDER_REGION_TOO_LARGE_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.region_too_large");
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

    private EditBox editbox1, editbox2;

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
                RenderScreenTab.Type.PLAYER_RELATIVE_RENDER)));
        deselectedTabs.add(addWidget(new RenderScreenTab(bgPosLeft + 27, bgPosTop - 28,
                TAB_TEXTURES[2], TAB_TEXTURES[3], TAB_TITLES[1],
                this::renderAPR, this::onTabPressed, new ItemStack(Items.GRASS_BLOCK),
                RenderScreenTab.Type.ABSOLUTE_POSITION_RENDER)));

        // Check tab selection
        if (selectedTab == null){
            // Select the first one
            selectedTab = deselectedTabs.get(0);
        } else{
            // Replace with brand new of the same type
            for (RenderScreenTab tab : deselectedTabs){
                if (tab.type == selectedTab.type){
                    selectedTab = tab;
                    break;
                }
            }
        }
        // Set current tab state as active
        selectedTab.setSelected();
        // Remove selected tab from unselected list
        deselectedTabs.remove(selectedTab);

        // Editbox
        editbox1 = addWidget(new EditBox(this.font,bgPosLeft + 8, bgPosTop + 32,
                179, 16, Component.literal("editbox1")));
        editbox1.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        editbox1.setMaxLength(29);
        editbox2 = addWidget(new EditBox(this.font,bgPosLeft + 8, bgPosTop + 67,
                179, 16, Component.literal("editbox2")));
        editbox2.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        editbox2.setMaxLength(29);

        // Render button
        renderButton = addWidget(new RenderButton(bgPosLeft + bgWidth / 2 - 30,
                bgPosTop + 92,
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
            tab.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // Background texture
        guiGraphics.blit(BACKGROUND_TEXTURE, bgPosLeft, bgPosTop,0, 0,
                bgWidth, bgHeight, bgWidth, bgHeight);

        // Render selected tab
        selectedTab.render(guiGraphics, mouseX, mouseY, partialTicks);

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

        // Editbox 1
        guiGraphics.drawString(this.font, EDITBOX_TITLES[0], bgPosLeft + 12, bgPosTop + 19,
                0x404040, false);
        editbox1.render(guiGraphics, mouseX, mouseY, partialTicks);

        //Editbox 2
        guiGraphics.drawString(this.font, EDITBOX_TITLES[1], bgPosLeft + 12, bgPosTop + 54,
                0x404040, false);
        editbox2.render(guiGraphics, mouseX, mouseY, partialTicks);

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

        // Editbox 1
        guiGraphics.drawString(this.font, EDITBOX_TITLES[2], bgPosLeft + 12, bgPosTop + 19,
                0x404040, false);
        editbox1.render(guiGraphics, mouseX, mouseY, partialTicks);

        //Editbox 2
        guiGraphics.drawString(this.font, EDITBOX_TITLES[3], bgPosLeft + 12, bgPosTop + 54,
                0x404040, false);
        editbox2.render(guiGraphics, mouseX, mouseY, partialTicks);

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

        // Load input
        int x1, y1, z1, x2, y2, z2;
        try{
            // Editbox1
            StringReader r = new StringReader(editbox1.getValue());
            x1 = r.readInt();
            r.skipWhitespace();
            y1 = r.readInt();
            r.skipWhitespace();
            z1 = r.readInt();

            // Editbox2
            r = new StringReader(editbox2.getValue());
            x2 = r.readInt();
            r.skipWhitespace();
            y2 = r.readInt();
            r.skipWhitespace();
            z2 = r.readInt();
        }
        catch (Exception e){
            player.sendSystemMessage(RENDER_WRONG_INPUT_MSG);
            return;
        }

        try (FileWriters fileWriters = new FileWriters()){
            // Place min x/y/z into minPos and max x/y/z into maxPos
            int minX = Math.min(x1, x2);
            int minY = Math.min(y1, y2);
            int minZ = Math.min(z1, z2);
            int maxX = Math.max(x1, x2);
            int maxY = Math.max(y1, y2);
            int maxZ = Math.max(z1, z2);

            // Restrict region size
            if ((maxX - minX > 400) || (maxZ - minZ > 400)){
                player.sendSystemMessage(RENDER_REGION_TOO_LARGE_MSG);
                return;
            }

            // Min/max positions in region
            BlockPos posMin, posMax;
            if (selectedTab.type == RenderScreenTab.Type.PLAYER_RELATIVE_RENDER){
                // Add player position
                posMin = new BlockPos(player.getBlockX() + minX,
                        player.getBlockY() + minY,
                        player.getBlockZ() + minZ);
                posMax = new BlockPos(player.getBlockX() + maxX,
                        player.getBlockY() + maxY,
                        player.getBlockZ() + maxZ);
            }
            else{
                posMin = new BlockPos(minX, minY, minZ);
                posMax = new BlockPos(maxX, maxY, maxZ);
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
