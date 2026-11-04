package net.arthurllew.rendercube.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arthurllew.rendercube.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static net.arthurllew.rendercube.RenderCube.MODID;

@Environment(EnvType.CLIENT)
public class ConfigScreen extends Screen {
    /**
     * Screen background texture (main menu).
     */
    private static final ResourceLocation MENU_LIST_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/menu_list_background.png");
    /**
     * Screen background texture (in-game).
     */
    private static final ResourceLocation INWORLD_MENU_LIST_BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");
    /**
     * Block filters background texture.
     */
    protected static final ResourceLocation BLOCK_FILTERS_BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/render_screen.png");
    
    /**
     * Screen title.
     */
    protected static final Component SCREEN_TITLE =
            Component.translatable("gui." + MODID + ".screen.config.title");
    /**
     * Per chunk rendering checkbox text.
     */
    protected static final Component CHECKBOX_PER_CHUNK_RENDERING_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkbox.per_chunk_rendering");
    /**
     * Directional shading checkbox text.
     */
    protected static final Component CHECKBOX_DIRECTIONAL_SHADING_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkbox.dir_light");
    /**
     * Region border culling flags checkbox text.
     */
    protected static final Component CHECKBOXES_CULLING_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling");
    protected static final Component CHECKBOXES_CULLING_N_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling.n");
    protected static final Component CHECKBOXES_CULLING_W_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling.w");
    protected static final Component CHECKBOXES_CULLING_S_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling.s");
    protected static final Component CHECKBOXES_CULLING_E_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling.e");
    protected static final Component CHECKBOXES_CULLING_U_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling.u");
    protected static final Component CHECKBOXES_CULLING_D_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.checkboxes.culling.d");
    /**
     * Block filters title.
     */
    protected static final Component BLOCK_FILTERS_TITLE =
            Component.translatable("gui." + MODID + ".screen.config.blockfilters.title");
    /**
     * Save and exit button text.
     */
    protected static final Component BUTTON_SAVE_CONFIG_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.button.save_config");

    /**
     * Previous screen.
     */
    private final Screen parent;

    /**
     * Draft of config data.
     */
    private final Config.Data configDraft = new Config.Data(Config.DATA);

    /**
     * Checkbox for controlling per-chunk geometry data saving.
     */
    protected Checkbox checkboxPerChunkRendering;
    /**
     * Checkbox for controlling capture of directional light.
     */
    protected Checkbox checkboxDirectionalShading;
    /**
     * Checkboxes for controlling region border culling flags.
     */
    protected Checkbox checkboxRegionBorderUnculledDirectionsN, checkboxRegionBorderUnculledDirectionsW,
            checkboxRegionBorderUnculledDirectionsS, checkboxRegionBorderUnculledDirectionsE,
            checkboxRegionBorderUnculledDirectionsU, checkboxRegionBorderUnculledDirectionsD;

    /**
     * Block filters background texture dimensions.
     */
    protected final int blockFiltersBgWidth = 195, blockFiltersBgHeight = 160;
    /**
     * Block filters background texture positions.
     */
    protected int blockFiltersBgPosLeft, blockFiltersBgPosTop;

    /**
     * Block filters title positions.
     */
    int blockFiltersTitleTextPosX, blockFiltersTitleTextPosY;

    /**
     * List block filters.
     */
    protected BlockFiltersList blockFiltersList;

    /**
     * Config save button.
     */
    protected Button buttonSaveConfig;

    /**
     * Constructor.
     */
    public ConfigScreen(Screen parent) {
        super(SCREEN_TITLE);
        this.parent = parent;
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
     * Initializes GUI element.
     */
    @Override
    protected void init() {
        super.init();

        // Clear previous widgets
        this.clearWidgets();

        // Checkbox for controlling per-chunk vertex data saving
        this.checkboxPerChunkRendering = addWidget(Checkbox
                .builder(CHECKBOX_PER_CHUNK_RENDERING_TEXT, this.font)
                .pos(10, 42)
                .maxWidth(180)
                .selected(configDraft.perChunkRendering)
                .onValueChange((c, v) -> configDraft.perChunkRendering = v)
                .build());

        // Checkbox for controlling capture of directional light
        this.checkboxDirectionalShading = addWidget(Checkbox
                .builder(CHECKBOX_DIRECTIONAL_SHADING_TEXT, this.font)
                .pos(10, 62)
                .maxWidth(180)
                .selected(configDraft.useDirectionalShading)
                .onValueChange((c, v) -> configDraft.useDirectionalShading = v)
                .build());

        // Checkboxes for controlling region border culling flags
        this.checkboxRegionBorderUnculledDirectionsN = addWidget(Checkbox
                .builder(CHECKBOXES_CULLING_N_TEXT, this.font)
                .pos(10, 102)
                .maxWidth(180)
                .selected(configDraft.regionBorderUnculledDirections.get(Direction.NORTH))
                .onValueChange((c, v)
                        -> configDraft.regionBorderUnculledDirections.replace(Direction.NORTH, v))
                .build());
        this.checkboxRegionBorderUnculledDirectionsW = addWidget(Checkbox
                .builder(CHECKBOXES_CULLING_W_TEXT, this.font)
                .pos(36 + this.font.width(CHECKBOXES_CULLING_N_TEXT), 102)
                .maxWidth(180)
                .selected(configDraft.regionBorderUnculledDirections.get(Direction.WEST))
                .onValueChange((c, v)
                        -> configDraft.regionBorderUnculledDirections.replace(Direction.WEST, v))
                .build());
        this.checkboxRegionBorderUnculledDirectionsS = addWidget(Checkbox
                .builder(CHECKBOXES_CULLING_S_TEXT, this.font)
                .pos(10, 122)
                .maxWidth(180)
                .selected(configDraft.regionBorderUnculledDirections.get(Direction.SOUTH))
                .onValueChange((c, v)
                        -> configDraft.regionBorderUnculledDirections.replace(Direction.SOUTH, v))
                .build());
        this.checkboxRegionBorderUnculledDirectionsE = addWidget(Checkbox
                .builder(CHECKBOXES_CULLING_E_TEXT, this.font)
                .pos(36 + this.font.width(CHECKBOXES_CULLING_N_TEXT), 122)
                .maxWidth(180)
                .selected(configDraft.regionBorderUnculledDirections.get(Direction.EAST))
                .onValueChange((c, v)
                        -> configDraft.regionBorderUnculledDirections.replace(Direction.EAST, v))
                .build());
        this.checkboxRegionBorderUnculledDirectionsU = addWidget(Checkbox
                .builder(CHECKBOXES_CULLING_U_TEXT, this.font)
                .pos(10, 142)
                .maxWidth(180)
                .selected(configDraft.regionBorderUnculledDirections.get(Direction.UP))
                .onValueChange((c, v)
                        -> configDraft.regionBorderUnculledDirections.replace(Direction.UP, v))
                .build());
        this.checkboxRegionBorderUnculledDirectionsD = addWidget(Checkbox
                .builder(CHECKBOXES_CULLING_D_TEXT, this.font)
                .pos(36 + this.font.width(CHECKBOXES_CULLING_N_TEXT), 142)
                .maxWidth(180)
                .selected(configDraft.regionBorderUnculledDirections.get(Direction.DOWN))
                .onValueChange((c, v)
                        -> configDraft.regionBorderUnculledDirections.replace(Direction.DOWN, v))
                .build());

        // Set background texture aligned to the right edge of the screen
        this.blockFiltersBgPosLeft = (this.width - this.blockFiltersBgWidth) - 10;
        this.blockFiltersBgPosTop = (this.height - this.blockFiltersBgHeight) / 2;

        // Update block filters title positions (string is horizontally centered on block filters background)
        this.blockFiltersTitleTextPosX = this.blockFiltersBgPosLeft + this.blockFiltersBgWidth / 2 - this.font.width(BLOCK_FILTERS_TITLE) / 2;
        this.blockFiltersTitleTextPosY = this.blockFiltersBgPosTop + 6;

        // Init list of block filters
        this.blockFiltersList = addWidget(
                new BlockFiltersList(this.minecraft, this.font, configDraft,
                        this.blockFiltersBgWidth - 10, this.blockFiltersBgHeight - 24,
                        this.blockFiltersBgPosTop + 18));
        this.blockFiltersList.setX(this.blockFiltersBgPosLeft + 5);

        // Config save button
        this.buttonSaveConfig = addWidget(Button
                .builder(BUTTON_SAVE_CONFIG_TEXT, b -> {
                    // Update config
                    Config.DATA = configDraft;
                    // Save config
                    Config.tryToSaveConfig();
                    // Close the screen
                    this.onClose();
                })
                .bounds(this.width / 2 - 50, this.height - 26, 100, 20)
                .build());
    }

    /**
     * Draws Controls-like screen background.
     */
    @SuppressWarnings("SameParameterValue")
    private void renderDarkArea(GuiGraphics guiGraphics, int x, int y, int w, int h) {
        //noinspection DataFlowIssue
        boolean menu = this.minecraft.level == null;
        // Needed for lines
        RenderSystem.enableBlend();
        // Tiled dark background
        guiGraphics.blit(menu ? MENU_LIST_BACKGROUND : INWORLD_MENU_LIST_BACKGROUND,
                x, y, (float) (x + w), (float) (y + h), w, h, 32, 32);
        // Line above and below (drawn outside the area)
        guiGraphics.blit(menu ? Screen.HEADER_SEPARATOR : Screen.INWORLD_HEADER_SEPARATOR,
                x, y - 2, 0f, 0f, w, 2, 32, 2);
        guiGraphics.blit(menu ? Screen.FOOTER_SEPARATOR : Screen.INWORLD_FOOTER_SEPARATOR,
                x, y + h, 0f, 0f, w, 2, 32, 2);
        // Stop blending
        RenderSystem.disableBlend();
    }

    /**
     * Renders GUI element.
     * @param guiGraphics GUI renderer
     * @param mouseX      X coordinate of the mouse cursor
     * @param mouseY      Y coordinate of the mouse cursor
     * @param partialTick partial tick
     */
    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Render background blur
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // Render background
        renderDarkArea(guiGraphics, 0, 33, this.width, this.height - 66);

        // Render screen title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        // Per chunk rendering checkbox
        checkboxPerChunkRendering.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render checkbox for controlling capture of directional light
        this.checkboxDirectionalShading.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render checkboxes for controlling region border culling flags
        guiGraphics.drawString(this.font, CHECKBOXES_CULLING_TEXT, 10, 86, 0xFFFFFF);
        this.checkboxRegionBorderUnculledDirectionsN.render(guiGraphics, mouseX, mouseY, partialTick);
        this.checkboxRegionBorderUnculledDirectionsW.render(guiGraphics, mouseX, mouseY, partialTick);
        this.checkboxRegionBorderUnculledDirectionsS.render(guiGraphics, mouseX, mouseY, partialTick);
        this.checkboxRegionBorderUnculledDirectionsE.render(guiGraphics, mouseX, mouseY, partialTick);
        this.checkboxRegionBorderUnculledDirectionsU.render(guiGraphics, mouseX, mouseY, partialTick);
        this.checkboxRegionBorderUnculledDirectionsD.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render block filters background texture
        guiGraphics.blit(BLOCK_FILTERS_BACKGROUND_TEXTURE, this.blockFiltersBgPosLeft, this.blockFiltersBgPosTop,0, 0,
                this.blockFiltersBgWidth, this.blockFiltersBgHeight, this.blockFiltersBgWidth, this.blockFiltersBgHeight);

        // Render block filters title
        guiGraphics.drawString(this.font, BLOCK_FILTERS_TITLE,
                this.blockFiltersTitleTextPosX, this.blockFiltersTitleTextPosY,
                0x404040, false);

        // Render list of block filters
        this.blockFiltersList.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render config save button
        this.buttonSaveConfig.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /**
     * Called before closing the screen.
     */
    @Override
    public void onClose() {
        // Go to the previous screen
        //noinspection DataFlowIssue
        this.minecraft.setScreen(parent);
    }
}
