package net.arthurllew.rendercube.client.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.arthurllew.rendercube.RenderCube;
import net.arthurllew.rendercube.client.rendering.CubesRenderer;
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

import java.util.List;
import java.util.function.Supplier;

import static net.arthurllew.rendercube.RenderCube.MODID;

@OnlyIn(Dist.CLIENT)
public class RenderScreen extends Screen {
    // Resources
    private static final ResourceLocation BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/render_screen.png");
    private static final ResourceLocation[] TAB_TEXTURES =
            new ResourceLocation[]{
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_right_selected.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_right_unselected.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_middle_selected.png"),
                    ResourceLocation.fromNamespaceAndPath(MODID,
                            "textures/gui/tab_top_middle_unselected.png")};
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
    private static final Component REGION_BOARDER_CHECKBOX_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.checkbox.region_boarder");
    private static final Component PER_CHUNK_RENDERING_CHECKBOX_TEXT =
            Component.translatable("gui." + MODID + ".render_screen.checkbox.per_chunk_rendering");
    private static final Component RENDER_WRONG_INPUT_MSG =
            Component.translatable("gui." + MODID + ".render_screen.button.render.wrong_input");
    private static final Supplier<Component> RENDER_REGION_TOO_LARGE_MSG = () ->
            Component.translatable("gui." + MODID + ".render_screen.button.render.region_too_large",
                    Config.DATA.maxRenderDistance);
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
     * Render button.
     */
    private RenderButton buttonRender;

    /**
     * Editboxes for coordinates input.
     */
    private EditBox editboxRenderPos1, editboxRenderPos2;

    /**
     * Checkbox for controlling region boarder face culling.
     */
    private Checkbox checkboxRenderRegionBoarderFaceCulling;

    /**
     * Checkbox for controlling per-chunk vertex data saving.
     */
    private Checkbox checkboxUsePerChunkRendering;

    /**
     * Screen state (is it rendering or not).
     */
    private boolean isIdle = true;

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

        // Populate tabs
        this.deselectedTabs.add(addWidget(new RenderScreenTab(this.bgPosLeft, this.bgPosTop - 28,
                TAB_TEXTURES[0], TAB_TEXTURES[1], TAB_TITLES[0],
                this::renderPRR, this::onTabPressed, new ItemStack(Items.PLAYER_HEAD),
                RenderScreenTab.Type.PLAYER_RELATIVE_RENDER)));
        this.deselectedTabs.add(addWidget(new RenderScreenTab(this.bgPosLeft + 27, this.bgPosTop - 28,
                TAB_TEXTURES[2], TAB_TEXTURES[3], TAB_TITLES[1],
                this::renderAPR, this::onTabPressed, new ItemStack(Items.GRASS_BLOCK),
                RenderScreenTab.Type.ABSOLUTE_POSITION_RENDER)));

        // Check tab selection
        if (selectedTab == null){
            // Select the first one
            selectedTab = this.deselectedTabs.getFirst();
        } else{
            // Replace with brand new of the same type
            for (RenderScreenTab tab : this.deselectedTabs){
                if (tab.type == selectedTab.type){
                    selectedTab = tab;
                    break;
                }
            }
        }
        // Set current tab state as active
        selectedTab.setSelected();
        // Remove selected tab from unselected list
        this.deselectedTabs.remove(selectedTab);

        // Editbox
        this.editboxRenderPos1 = addWidget(new EditBox(this.font,this.bgPosLeft + 8, this.bgPosTop + 32,
                179, 16, Component.literal("editbox1")));
        this.editboxRenderPos1.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        this.editboxRenderPos1.setMaxLength(29);
        this.editboxRenderPos2 = addWidget(new EditBox(this.font,this.bgPosLeft + 8, this.bgPosTop + 67,
                179, 16, Component.literal("editbox2")));
        this.editboxRenderPos2.setTooltip(Tooltip.create(EDITBOX_TOOLTIP));
        this.editboxRenderPos2.setMaxLength(29);

        // Render button
        this.buttonRender = addWidget(new RenderButton(
                        this.bgPosLeft + this.bgWidth / 2 - 30, this.bgPosTop + 88,
                        60, 20,
                        RENDER_BUTTON_TEXT,
                        this::onRenderButtonPressed));

        // Checkbox for controlling region boarder face culling
        this.checkboxRenderRegionBoarderFaceCulling = addWidget(Checkbox.builder(Component.literal(""), this.font)
                .pos(this.bgPosLeft + 8, this.bgPosTop + 111)
                .maxWidth(180).selected(false).build());

        // Checkbox for controlling per-chunk vertex data saving
        this.checkboxUsePerChunkRendering = addWidget(Checkbox.builder(Component.literal(""), this.font)
                .pos(this.bgPosLeft + 8, this.bgPosTop + 134)
                .maxWidth(180).selected(false).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

        // Render all unselected tabs under background texture
        for(RenderScreenTab tab : this.deselectedTabs){
            tab.render(guiGraphics, mouseX, mouseY, partialTicks);
        }

        // Background texture
        guiGraphics.blit(BACKGROUND_TEXTURE, this.bgPosLeft, this.bgPosTop,0, 0,
                this.bgWidth, this.bgHeight, this.bgWidth, this.bgHeight);

        // Render selected tab
        selectedTab.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Render current tab contents
        selectedTab.renderMethod.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Render button
        this.buttonRender.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Region boarder checkbox
        guiGraphics.drawString(this.font, REGION_BOARDER_CHECKBOX_TEXT,
                this.bgPosLeft + Checkbox.getBoxSize(this.font) + 12,
                this.bgPosTop + Checkbox.getBoxSize(this.font) / 2 + 108,
                0x404040, false);
        this.checkboxRenderRegionBoarderFaceCulling.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Data writers checkbox
        guiGraphics.drawString(this.font, PER_CHUNK_RENDERING_CHECKBOX_TEXT,
                this.bgPosLeft + Checkbox.getBoxSize(this.font) + 12,
                this.bgPosTop + Checkbox.getBoxSize(this.font) / 2 + 131,
                0x404040, false);
        this.checkboxUsePerChunkRendering.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders player relative render tab.
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderPRR(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Tab title
        guiGraphics.drawString(this.font, TAB_TITLES[0], this.bgPosLeft + 8, this.bgPosTop + 6,
                0x404040, false);

        // Editbox 1
        guiGraphics.drawString(this.font, EDITBOX_TITLES[0], this.bgPosLeft + 12, this.bgPosTop + 19,
                0x404040, false);
        this.editboxRenderPos1.render(guiGraphics, mouseX, mouseY, partialTicks);

        //Editbox 2
        guiGraphics.drawString(this.font, EDITBOX_TITLES[1], this.bgPosLeft + 12, this.bgPosTop + 54,
                0x404040, false);
        this.editboxRenderPos2.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders absolute position render tab.
     * @param guiGraphics the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderAPR(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // Tab title
        guiGraphics.drawString(this.font, TAB_TITLES[1], this.bgPosLeft + 8, this.bgPosTop + 6,
                0x404040, false);

        // Editbox 1
        guiGraphics.drawString(this.font, EDITBOX_TITLES[2], this.bgPosLeft + 12, this.bgPosTop + 19,
                0x404040, false);
        this.editboxRenderPos1.render(guiGraphics, mouseX, mouseY, partialTicks);

        // Editbox 2
        guiGraphics.drawString(this.font, EDITBOX_TITLES[3], this.bgPosLeft + 12, this.bgPosTop + 54,
                0x404040, false);
        this.editboxRenderPos2.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    /**
     * Handles tab being pressed.
     * @param tab pressed tab instance
     */
    private void onTabPressed(RenderScreenTab tab) {
        if (tab != selectedTab) {
            // Deselect current tab
            selectedTab.setUnselected();
            // Remove selected tab from unselected list
            this.deselectedTabs.remove(tab);
            // Add deselected tab to unselected list
            this.deselectedTabs.add(selectedTab);
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
    private void onRenderButtonPressed(AbstractButton button) {
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
                    if (selectedTab.type == RenderScreenTab.Type.PLAYER_RELATIVE_RENDER) {
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
                        CubesRenderer.captureRegion(checkboxUsePerChunkRendering.selected(),
                                player.level(), posMin, posMax,
                                checkboxRenderRegionBoarderFaceCulling.selected());

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
}
