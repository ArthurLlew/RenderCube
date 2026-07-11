package net.arthurllew.rendercube.client.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.rendering.CubesRenderer;
import net.arthurllew.rendercube.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Checkbox;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

import static net.arthurllew.rendercube.RenderCube.MODID;

@Environment(EnvType.CLIENT)
public class RenderScreen extends Screen {
    /**
     * Screen background texture.
     */
    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation(MODID, "textures/gui/render_screen.png");

    /**
     * Selected/deselected top right tab textures.
     */
    protected static final ResourceLocation[] TAB_TOP_RIGHT_TEXTURES =
            new ResourceLocation[]{
                    new ResourceLocation(MODID,
                            "textures/gui/tab_top_right_selected.png"),
                    new ResourceLocation(MODID,
                            "textures/gui/tab_top_right_unselected.png")};
    /**
     * Selected/deselected top middle tab textures.
     */
    protected static final ResourceLocation[] TAB_TOP_MIDDLE_TEXTURES =
            new ResourceLocation[]{
                    new ResourceLocation(MODID,
                            "textures/gui/tab_top_middle_selected.png"),
                    new ResourceLocation(MODID,
                            "textures/gui/tab_top_middle_unselected.png")};

    /**
     * Tab titles.
     */
    private static final Component[] TAB_TITLES = new Component[]{
            Component.translatable("gui." + MODID + ".render_screen.prr_tab"),
            Component.translatable("gui." + MODID + ".render_screen.apr_tab")};

    /**
     * Position input titles.
     */
    private static final Component[] EDITBOX_TITLES = new Component[]{
            Component.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos1"),
            Component.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos2"),
            Component.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos1"),
            Component.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos2")};

    /**
     * Position input common tooltip.
     */
    private static final Component EDITBOX_TOOLTIP =
            Component.translatable("gui." + MODID + ".render_screen.edit_box.tooltip");

    /**
     * Render button text.
     */
    private static final Component RENDER_BUTTON_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.button.render");

    /**
     * Region border culling checkbox text.
     */
    private static final Component REGION_BOARDER_CHECKBOX_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.checkbox.region_boarder");
    /**
     * Per chunk rendering checkbox text.
     */
    private static final Component PER_CHUNK_RENDERING_CHECKBOX_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.checkbox.per_chunk_rendering");

    /**
     * Incorrect input message.
     */
    private static final Component RENDER_WRONG_INPUT_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.wrong_input");
    /**
     * Exceeding render region size cap message.
     */
    private static final Supplier<Component> RENDER_REGION_TOO_LARGE_MSG = () ->
            Component.translatable("gui." + MODID + ".render_screen.button.render.region_too_large",
                    Config.DATA.maxRenderDistance);
    /**
     * Render success message.
     */
    private static final Component RENDER_SUCCESS_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.success");
    /**
     * Rendering error message.
     */
    private static final Component RENDER_ERROR_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.error");

    /**
     * Background texture dimensions.
     */
    protected final int bgWidth, bgHeight;
    /**
     * Background texture positions.
     */
    protected int bgPosLeft, bgPosTop;

    /**
     * Tabs list.
     */
    protected final List<RenderScreenTab> tabs = Lists.newArrayList();
    /**
     * Active tab.
     */
    protected RenderScreenTab selectedTab;

    /**
     * Render button.
     */
    protected RenderButton buttonRender;

    /**
     * Editboxes for coordinates input.
     */
    protected EditBox editboxRenderPos1, editboxRenderPos2;

    /**
     * Checkbox for controlling region boarder face culling.
     */
    protected Checkbox checkboxBoarderCulling;
    /**
     * Checkbox for controlling per-chunk geometry data saving.
     */
    protected Checkbox checkboxPerChunkRendering;

    /**
     * Screen state (is rendering or not).
     */
    protected boolean isIdle = true;

    /**
     * Constructor.
     */
    public RenderScreen() {
        super(CommonComponents.EMPTY);

        // Background texture size in pixels
        this.bgWidth = 195;
        this.bgHeight = 160;
    }

    /**
     * Tells whether game should be paused when opening this screen.
     * @return {@code true}.
     */
    @Override
    public boolean isPauseScreen() {
        return true;
    }

    /**
     * @return whether the player can close the screen with the ESC key.
     */
    @Override
    public boolean shouldCloseOnEsc() {
        return this.isIdle;
    }

    /**
     * Initializes GUI element.
     */
    @Override
    protected void init() {
        super.init();

        // Set background texture coordinates in screen center
        this.bgPosLeft = (this.width - this.bgWidth) / 2;
        this.bgPosTop = (this.height - this.bgHeight) / 2;

        // Refresh tabs list
        this.tabs.clear();
        this.tabs.add(addWidget(new TabPRR(this.bgPosLeft, this.bgPosTop - 28,
                TAB_TITLES[0], TAB_TOP_RIGHT_TEXTURES[0], TAB_TOP_RIGHT_TEXTURES[1], new ItemStack(Items.PLAYER_HEAD),
                this::onTabPressed)));
        this.tabs.add(addWidget(new TabAPR(this.bgPosLeft + 27, this.bgPosTop - 28,
                TAB_TITLES[1], TAB_TOP_MIDDLE_TEXTURES[0], TAB_TOP_MIDDLE_TEXTURES[1], new ItemStack(Items.GRASS_BLOCK),
                this::onTabPressed)));

        // Select the first tab
        this.selectedTab = this.tabs.get(0);
        this.selectedTab.select();

        EditBox prevEditbox;
        // Positions 1 input
        prevEditbox = this.editboxRenderPos1;
        this.editboxRenderPos1 = addWidget(new EditBox(this.font,this.bgPosLeft + 8, this.bgPosTop + 32,
                179, 16, Component.literal("editbox1")));
        this.editboxRenderPos1.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        this.editboxRenderPos1.setMaxLength(29);
        if (prevEditbox != null) {
            this.editboxRenderPos1.setValue(prevEditbox.getValue());
        }
        // Positions 2 input
        prevEditbox = this.editboxRenderPos2;
        this.editboxRenderPos2 = addWidget(new EditBox(this.font,this.bgPosLeft + 8, this.bgPosTop + 67,
                179, 16, Component.literal("editbox2")));
        this.editboxRenderPos2.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        this.editboxRenderPos2.setMaxLength(29);
        if (prevEditbox != null) {
            this.editboxRenderPos2.setValue(prevEditbox.getValue());
        }

        // Render button
        this.buttonRender = addWidget(new RenderButton(
                this.bgPosLeft + this.bgWidth / 2 - 30, this.bgPosTop + 88,
                60, 20,
                RENDER_BUTTON_TEXT,
                this::onRenderButtonPressed));

        // Checkbox for controlling region boarder face culling
        this.checkboxBoarderCulling = addWidget(
                new Checkbox(this.bgPosLeft + 5, this.bgPosTop + 111,
                        20, 20,
                        Component.literal(""),
                        this.checkboxBoarderCulling != null && this.checkboxBoarderCulling.selected()));

        // Checkbox for controlling per-chunk vertex data saving
        this.checkboxPerChunkRendering = addWidget(
                new Checkbox(this.bgPosLeft + 5, this.bgPosTop + 134,
                        20, 20,
                        Component.literal(""),
                        this.checkboxPerChunkRendering != null && this.checkboxPerChunkRendering.selected()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render background blur
        this.renderBackground(guiGraphics);

        // Render all unselected tabs under background texture
        for(RenderScreenTab tab : this.tabs){
            if (!tab.isSelected()) {
                tab.render(guiGraphics, mouseX, mouseY, partialTicks);
            }
        }

        // Render background texture
        guiGraphics.blit(BACKGROUND_TEXTURE, this.bgPosLeft, this.bgPosTop,0, 0,
                this.bgWidth, this.bgHeight, this.bgWidth, this.bgHeight);

        // Render selected tab
        this.selectedTab.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Handles tab being pressed.
     * @param tab pressed tab instance
     */
    protected void onTabPressed(RenderScreenTab tab) {
        if (tab != this.selectedTab) {
            // Deselect current tab
            this.selectedTab.deselect();
            // Assign new selected tab
            this.selectedTab = tab;
            // Set its state as active
            this.selectedTab.select();
        }
    }

    /**
     * Handles render button being pressed.
     * @param button pressed button instance
     */
    protected void onRenderButtonPressed(AbstractButton button) {
        // Only render if not already rendering
        if (this.isIdle) {
            // Seal screen
            this.isIdle = false;

            // Safely get minecraft player
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {throw new UnsupportedOperationException("Player is null");}

            try {
                // Load input from Editbox1
                StringReader r = new StringReader(this.editboxRenderPos1.getValue());
                int x1 = r.readInt();
                r.skipWhitespace();
                int y1 = r.readInt();
                r.skipWhitespace();
                int z1 = r.readInt();

                // Load input from Editbox2
                r = new StringReader(this.editboxRenderPos2.getValue());
                int x2 = r.readInt();
                r.skipWhitespace();
                int y2 = r.readInt();
                r.skipWhitespace();
                int z2 = r.readInt();

                // Place min x/y/z into minPos and max x/y/z into maxPos
                int minX = Math.min(x1, x2);
                int minY = Math.min(y1, y2);
                int minZ = Math.min(z1, z2);
                int maxX = Math.max(x1, x2);
                int maxY = Math.max(y1, y2);
                int maxZ = Math.max(z1, z2);

                // Restrict region size
                if ((maxX - minX > Config.DATA.maxRenderDistance)
                        || (maxZ - minZ > Config.DATA.maxRenderDistance)) {
                    player.sendSystemMessage(RENDER_REGION_TOO_LARGE_MSG.get());
                }
                else {
                    // Min/max positions in region
                    BlockPos posMin, posMax;
                    if (this.selectedTab instanceof TabPRR) {
                        // Add player position
                        posMin = new BlockPos(player.getBlockX() + minX,
                                player.getBlockY() + minY,
                                player.getBlockZ() + minZ);
                        posMax = new BlockPos(player.getBlockX() + maxX,
                                player.getBlockY() + maxY,
                                player.getBlockZ() + maxZ);
                    }
                    else {
                        posMin = new BlockPos(minX, minY, minZ);
                        posMax = new BlockPos(maxX, maxY, maxZ);
                    }

                    // Wrap rendering to prevent game crash
                    try {
                        // Render requested region
                        CubesRenderer.captureRegion(checkboxPerChunkRendering.selected(),
                                player.level(), posMin, posMax,
                                checkboxBoarderCulling.selected());

                        // Notify about success
                        player.sendSystemMessage(RENDER_SUCCESS_MSG);
                    }
                    catch(Exception e) {
                        // Render error
                        RenderCube.LOGGER.error("RenderCube encountered error while rendering", e);
                        player.sendSystemMessage(RENDER_ERROR_MSG);
                    }
                }
            }
            catch (CommandSyntaxException e) {
                // Input error
                player.sendSystemMessage(RENDER_WRONG_INPUT_MSG);
            }

            // Unseal screen
            this.isIdle = true;
        }
    }

    /**
     * Basic rendering tab.
     */
    protected class RenderScreenTabBasic extends RenderScreenTab {
        /**
         * Constructor.
         * @param posX X position on screen
         * @param posY Y position on screen
         * @param title tab title
         * @param selectedTexture tab texture when active
         * @param unselectedTexture tab texture when non-active
         * @param itemIcon tab item icon
         * @param onClick action, performed when tab is clicked
         */
        RenderScreenTabBasic(int posX, int posY, Component title,
                             ResourceLocation selectedTexture, ResourceLocation unselectedTexture, ItemStack itemIcon,
                             OnClick onClick) {
            super(posX, posY, title, selectedTexture, unselectedTexture, itemIcon, onClick);
        }

        /**
         * Renders player relative render tab.
         * @param guiGraphics GUI renderer
         * @param mouseX X coordinate of the mouse cursor
         * @param mouseY Y coordinate of the mouse cursor
         * @param partialTicks partial tick time
         */
        public void renderTabContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Render button
            buttonRender.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Region boarder culling checkbox
            guiGraphics.drawString(font, REGION_BOARDER_CHECKBOX_TEXT,
                    bgPosLeft + checkboxBoarderCulling.getHeight() + 9,
                    bgPosTop + checkboxBoarderCulling.getHeight() / 2 + 108,
                    0x404040, false);
            checkboxBoarderCulling.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Per chunk rendering checkbox
            guiGraphics.drawString(font, PER_CHUNK_RENDERING_CHECKBOX_TEXT,
                    bgPosLeft + checkboxPerChunkRendering.getHeight() + 9,
                    bgPosTop + checkboxPerChunkRendering.getHeight() / 2 + 131,
                    0x404040, false);
            checkboxPerChunkRendering.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Player relative render tab.
     */
    protected class TabPRR extends RenderScreenTabBasic {
        /**
         * Constructor.
         * @param posX X position on screen
         * @param posY Y position on screen
         * @param title tab title
         * @param selectedTexture tab texture when active
         * @param unselectedTexture tab texture when non-active
         * @param itemIcon tab item icon
         * @param onClick action, performed when tab is clicked
         */
        TabPRR(int posX, int posY, Component title,
               ResourceLocation selectedTexture, ResourceLocation unselectedTexture, ItemStack itemIcon,
               OnClick onClick) {
            super(posX, posY, title, selectedTexture, unselectedTexture, itemIcon, onClick);
        }

        /**
         * Renders player relative render tab.
         * @param guiGraphics GUI renderer
         * @param mouseX X coordinate of the mouse cursor
         * @param mouseY Y coordinate of the mouse cursor
         * @param partialTicks partial tick time
         */
        public void renderTabContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Render generic contents
            super.renderTabContents(guiGraphics, mouseX, mouseY, partialTicks);

            // Tab title
            guiGraphics.drawString(font, TAB_TITLES[0], bgPosLeft + 8, bgPosTop + 6,
                    0x404040, false);

            // Editbox 1
            guiGraphics.drawString(font, EDITBOX_TITLES[0], bgPosLeft + 12, bgPosTop + 19,
                    0x404040, false);
            editboxRenderPos1.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Editbox 2
            guiGraphics.drawString(font, EDITBOX_TITLES[1], bgPosLeft + 12, bgPosTop + 54,
                    0x404040, false);
            editboxRenderPos2.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Absolute position render tab.
     */
    protected class TabAPR extends RenderScreenTabBasic {
        /**
         * Constructor.
         * @param posX X position on screen
         * @param posY Y position on screen
         * @param title tab title
         * @param selectedTexture tab texture when active
         * @param unselectedTexture tab texture when non-active
         * @param itemIcon tab item icon
         * @param onClick action, performed when tab is clicked
         */
        TabAPR(int posX, int posY, Component title,
               ResourceLocation selectedTexture, ResourceLocation unselectedTexture, ItemStack itemIcon,
               OnClick onClick) {
            super(posX, posY, title, selectedTexture, unselectedTexture, itemIcon, onClick);
        }

        /**
         * Renders absolute position render tab.
         * @param guiGraphics the GuiGraphics object used for rendering.
         * @param mouseX the x-coordinate of the mouse cursor.
         * @param mouseY the y-coordinate of the mouse cursor.
         * @param partialTicks the partial tick time.
         */
        public void renderTabContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Render generic contents
            super.renderTabContents(guiGraphics, mouseX, mouseY, partialTicks);

            // Tab title
            guiGraphics.drawString(font, TAB_TITLES[1], bgPosLeft + 8, bgPosTop + 6,
                    0x404040, false);

            // Editbox 1
            guiGraphics.drawString(font, EDITBOX_TITLES[2], bgPosLeft + 12, bgPosTop + 19,
                    0x404040, false);
            editboxRenderPos1.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Editbox 2
            guiGraphics.drawString(font, EDITBOX_TITLES[3], bgPosLeft + 12, bgPosTop + 54,
                    0x404040, false);
            editboxRenderPos2.render(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }
}
