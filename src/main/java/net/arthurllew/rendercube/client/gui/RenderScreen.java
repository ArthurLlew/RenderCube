package net.arthurllew.rendercube.client.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
import net.arthurllew.rendercube.client.rendering.chunk.ChunkRendererVanilla;
import net.arthurllew.rendercube.config.Config;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import static net.arthurllew.rendercube.RenderCube.MODID;

@OnlyIn(Dist.CLIENT)
public class RenderScreen extends Screen {
    /**
     * Screen background texture.
     */
    protected static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/render_screen.png");

    /**
     * Selected/deselected top right tab textures.
     */
    protected static final ResourceLocation[] TAB_TOP_RIGHT_TEXTURES =
            new ResourceLocation[]{
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_right_selected.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_right_unselected.png")};
    /**
     * Selected/deselected top middle tab textures.
     */
    protected static final ResourceLocation[] TAB_TOP_MIDDLE_TEXTURES =
            new ResourceLocation[]{
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_middle_selected.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_middle_unselected.png")};

    /**
     * Position input common tooltip.
     */
    protected static final Component EDITBOX_TOOLTIP =
            Component.translatable("gui." + MODID + ".render_screen.edit_box.tooltip");

    /**
     * Render button text.
     */
    protected static final Component RENDER_BUTTON_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.button.render");
    /**
     * Region border culling checkbox text.
     */
    protected static final Component REGION_BOARDER_CHECKBOX_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.checkbox.region_boarder");
    /**
     * Per chunk rendering checkbox text.
     */
    protected static final Component PER_CHUNK_RENDERING_CHECKBOX_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.checkbox.per_chunk_rendering");

    /**
     * Incorrect input message.
     */
    protected static final Component RENDER_WRONG_INPUT_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.wrong_input");
    /**
     * Exceeding render region size cap message.
     */
    protected static final Supplier<Component> RENDER_REGION_TOO_LARGE_MSG = () ->
            Component.translatable("gui." + MODID + ".render_screen.button.render.region_too_large",
                    Config.DATA.maxRenderDistance);
    /**
     * Render success message.
     */
    protected static final Component RENDER_SUCCESS_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.success");
    /**
     * Rendering error message.
     */
    protected static final Component RENDER_ERROR_MSG =
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
    protected final List<Tab> tabs = Lists.newArrayList();
    /**
     * Active tab.
     */
    protected Tab selectedTab;

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

        EditBox prevEditbox;
        // Positions 1 input
        prevEditbox = this.editboxRenderPos1;
        this.editboxRenderPos1 = addWidget(new EditBox(this.font,
                this.bgPosLeft + 8, this.bgPosTop + 32,
                179, 16, Component.literal("editbox1")));
        this.editboxRenderPos1.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        this.editboxRenderPos1.setMaxLength(29);
        if (prevEditbox != null) {
            this.editboxRenderPos1.setValue(prevEditbox.getValue());
        }
        // Positions 2 input
        prevEditbox = this.editboxRenderPos2;
        this.editboxRenderPos2 = addWidget(new EditBox(this.font,
                this.bgPosLeft + 8, this.bgPosTop + 67,
                179, 16, Component.literal("editbox2")));
        this.editboxRenderPos2.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        this.editboxRenderPos2.setMaxLength(29);
        if (prevEditbox != null) {
            this.editboxRenderPos2.setValue(prevEditbox.getValue());
        }

        // Checkbox for controlling region boarder face culling
        this.checkboxBoarderCulling = addWidget(Checkbox
                .builder(Component.literal(""), this.font)
                .pos(this.bgPosLeft + 9, this.bgPosTop + 88)
                .maxWidth(180)
                .selected(this.checkboxBoarderCulling != null && this.checkboxBoarderCulling.selected())
                .build());

        // Checkbox for controlling per-chunk vertex data saving
        this.checkboxPerChunkRendering = addWidget(Checkbox
                .builder(Component.literal(""), this.font)
                .pos(this.bgPosLeft + 9, this.bgPosTop + 112)
                .maxWidth(180)
                .selected(this.checkboxPerChunkRendering != null && this.checkboxPerChunkRendering.selected())
                .build());

        // Render button
        this.buttonRender = addWidget(new RenderButton(
                        this.bgPosLeft + this.bgWidth / 2 - 30, this.bgPosTop + 133,
                        60, 20,
                        RENDER_BUTTON_TEXT,
                        this::onRenderButtonPressed));

        // Refresh tabs list
        this.tabs.clear();
        this.tabs.add(addWidget(new TabPRR(this.bgPosLeft, this.bgPosTop - 28,
                Component.translatable("gui." + MODID + ".render_screen.prr.title"),
                Component.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos1"),
                Component.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos2"),
                TAB_TOP_RIGHT_TEXTURES[0], TAB_TOP_RIGHT_TEXTURES[1], new ItemStack(Items.PLAYER_HEAD),
                this::onTabPressed)));
        this.tabs.add(addWidget(new TabAPR(this.bgPosLeft + 27, this.bgPosTop - 28,
                Component.translatable("gui." + MODID + ".render_screen.apr.title"),
                Component.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos1"),
                Component.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos2"),
                TAB_TOP_MIDDLE_TEXTURES[0], TAB_TOP_MIDDLE_TEXTURES[1], new ItemStack(Items.GRASS_BLOCK),
                this::onTabPressed)));
        // Select the first tab
        this.selectedTab = this.tabs.getFirst();
        this.selectedTab.select();
    }

    /**
     * Renders GUI element.
     * @param guiGraphics  GUI renderer
     * @param mouseX       X coordinate of the mouse cursor
     * @param mouseY       Y coordinate of the mouse cursor
     * @param partialTicks partial tick time
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Render background blur
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Render all unselected tabs under background texture
        for(Tab tab : this.tabs){
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
            this.selectedTab = (Tab)tab;
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
                    // Wrap rendering to prevent game crash
                    try {
                        // Render requested region
                        this.selectedTab.captureRegion(player,
                                new BlockPos(minX, minY, minZ),
                                new BlockPos(maxX, maxY, maxZ));

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
    protected abstract class Tab extends RenderScreenTab {
        // Tab specific texts.
        protected final Component title, editbox1Text, editbox2Text;

        // Pre-computed UI positions
        final int titleTextPosX = bgPosLeft + 8;
        final int titleTextPosY = bgPosTop + 6;
        final int editboxTextPosX = bgPosLeft + 12;
        final int editbox1TextPosY = bgPosTop + 19;
        final int editbox2TextPosY = bgPosTop + 54;
        final int checkBoxTextPosX = bgPosLeft + Checkbox.getBoxSize(font) + 12;
        final int checkBox1TextPosY = checkboxBoarderCulling.getY() + Checkbox.getBoxSize(font) / 2 - 3;
        final int checkBox2TextPosY = checkboxPerChunkRendering.getY() + Checkbox.getBoxSize(font) / 2 - 3;

        /**
         * Constructor.
         * @param posX              X position on screen
         * @param posY              Y position on screen
         * @param title             tab title and tooltip text
         * @param editbox1Text      text of the first editbox
         * @param editbox2Text      text of the second editbox
         * @param selectedTexture   tab texture when active
         * @param unselectedTexture tab texture when non-active
         * @param itemIcon          tab item icon
         * @param onClick           action, performed when tab is clicked
         */
        Tab(int posX, int posY, Component title, Component editbox1Text, Component editbox2Text,
            ResourceLocation selectedTexture, ResourceLocation unselectedTexture, ItemStack itemIcon,
            OnClick onClick) {
            super(posX, posY, title, selectedTexture, unselectedTexture, itemIcon, onClick);
            this.title = title;
            this.editbox1Text = editbox1Text;
            this.editbox2Text = editbox2Text;
        }

        /**
         * Renders player relative render tab.
         * @param guiGraphics  GUI renderer
         * @param mouseX       X coordinate of the mouse cursor
         * @param mouseY       Y coordinate of the mouse cursor
         * @param partialTicks partial tick time
         */
        @Override
        public void renderTabContents(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            // Tab title
            guiGraphics.drawString(font, title, titleTextPosX, titleTextPosY, 0x404040, false);

            // Editbox 1
            guiGraphics.drawString(font, editbox1Text, this.editboxTextPosX, this.editbox1TextPosY,
                    0x404040, false);
            editboxRenderPos1.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Editbox 2
            guiGraphics.drawString(font, editbox2Text, this.editboxTextPosX, this.editbox2TextPosY,
                    0x404040, false);
            editboxRenderPos2.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Region boarder culling checkbox
            guiGraphics.drawString(font, REGION_BOARDER_CHECKBOX_TEXT,
                    this.checkBoxTextPosX, this.checkBox1TextPosY,
                    0x404040, false);
            checkboxBoarderCulling.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Per chunk rendering checkbox
            guiGraphics.drawString(font, PER_CHUNK_RENDERING_CHECKBOX_TEXT,
                    this.checkBoxTextPosX, this.checkBox2TextPosY,
                    0x404040, false);
            checkboxPerChunkRendering.render(guiGraphics, mouseX, mouseY, partialTicks);

            // Render button
            buttonRender.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        /**
         * Tab specific world region capture method.
         * @param player client player
         * @param minPos min block position of the region to capture
         * @param maxPos max block position of the region to capture
         */
        public abstract void captureRegion(@NotNull LocalPlayer player,
                                           @NotNull BlockPos minPos,
                                           @NotNull BlockPos maxPos) throws IOException;
    }

    /**
     * Player relative render tab.
     */
    protected class TabPRR extends Tab {
        /**
         * Constructor.
         * @param posX              X position on screen
         * @param posY              Y position on screen
         * @param title             tab title and tooltip text
         * @param editbox1Text      text of the first editbox
         * @param editbox2Text      text of the second editbox
         * @param selectedTexture   tab texture when active
         * @param unselectedTexture tab texture when non-active
         * @param itemIcon          tab item icon
         * @param onClick           action, performed when tab is clicked
         */
        TabPRR(int posX, int posY, Component title, Component editbox1Text, Component editbox2Text,
               ResourceLocation selectedTexture, ResourceLocation unselectedTexture, ItemStack itemIcon,
               OnClick onClick) {
            super(posX, posY, title, editbox1Text, editbox2Text, selectedTexture, unselectedTexture, itemIcon, onClick);
        }

        /**
         * Captures world region relative to the player position.
         * @param player client player
         * @param minPos min block position of the region to capture
         * @param maxPos max block position of the region to capture
         */
        @Override
        public void captureRegion(@NotNull LocalPlayer player,
                                  @NotNull BlockPos minPos,
                                  @NotNull BlockPos maxPos) throws IOException {
            // Render requested region with position offset by player coordinates
            RegionRenderer.captureRegion(new ChunkRendererVanilla(player.level()),
                    minPos.offset(player.getBlockX(), player.getBlockY(), player.getBlockZ()),
                    maxPos.offset(player.getBlockX(), player.getBlockY(), player.getBlockZ()),
                    checkboxPerChunkRendering.selected(),
                    checkboxBoarderCulling.selected());
        }
    }

    /**
     * Absolute position render tab.
     */
    protected class TabAPR extends Tab {
        /**
         * Constructor.
         * @param posX              X position on screen
         * @param posY              Y position on screen
         * @param title             tab title and tooltip text
         * @param editbox1Text      text of the first editbox
         * @param editbox2Text      text of the second editbox
         * @param selectedTexture   tab texture when active
         * @param unselectedTexture tab texture when non-active
         * @param itemIcon          tab item icon
         * @param onClick           action, performed when tab is clicked
         */
        TabAPR(int posX, int posY, Component title, Component editbox1Text, Component editbox2Text,
               ResourceLocation selectedTexture, ResourceLocation unselectedTexture, ItemStack itemIcon,
               OnClick onClick) {
            super(posX, posY, title, editbox1Text, editbox2Text, selectedTexture, unselectedTexture, itemIcon, onClick);
        }

        /**
         * Captures world region.
         * @param player client player
         * @param minPos min block position of the region to capture
         * @param maxPos max block position of the region to capture
         */
        @Override
        public void captureRegion(@NotNull LocalPlayer player,
                                  @NotNull BlockPos minPos,
                                  @NotNull BlockPos maxPos) throws IOException {
            // Render requested region
            RegionRenderer.captureRegion(new ChunkRendererVanilla(player.level()),
                    minPos, maxPos,
                    checkboxPerChunkRendering.selected(),
                    checkboxBoarderCulling.selected());
        }
    }
}
