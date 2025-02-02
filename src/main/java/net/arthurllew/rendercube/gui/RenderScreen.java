package net.arthurllew.rendercube.gui;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import net.arthurllew.rendercube.rendering.FileWriters;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

import static net.arthurllew.rendercube.RenderCube.MODID;
import static net.arthurllew.rendercube.rendering.CubesRenderer.renderRegion;

@Environment(value=EnvType.CLIENT)
public class RenderScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();

    // Resources
    private static final Identifier BACKGROUND_TEXTURE =
            new Identifier(MODID, "textures/gui/render_screen.png");
    private static final Identifier[] TAB_TEXTURES =
            new Identifier[]{
                    new Identifier(MODID, "textures/gui/tab_top_right_selected.png"),
                    new Identifier(MODID, "textures/gui/tab_top_right_unselected.png"),
                    new Identifier(MODID, "textures/gui/tab_top_middle_selected.png"),
                    new Identifier(MODID, "textures/gui/tab_top_middle_unselected.png")};
    private static final Text[] TAB_TITLES = new Text[]{
            Text.translatable("gui." + MODID + ".render_screen.prr_tab"),
            Text.translatable("gui." + MODID + ".render_screen.apr_tab")};
    private static final Text[] EDITBOX_TITLES = new Text[]{
            Text.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos1"),
            Text.translatable("gui." + MODID + ".render_screen.prr.edit_box.pos2"),
            Text.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos1"),
            Text.translatable("gui." + MODID + ".render_screen.apr.edit_box.pos2")};
    private static final Text EDITBOX_TOOLTIP =
            Text.translatable("gui." + MODID + ".render_screen.edit_box.tooltip");
    private static final Text RENDER_BUTTON_TEXT =
            Text.translatable("gui." + MODID + ".render_screen.button.render");
    private static final Text RENDER_WRONG_INPUT_MSG =
            Text.translatable("gui." + MODID + ".render_screen.button.render.wrong_input");
    private static final Text RENDER_REGION_TOO_LARGE_MSG =
            Text.translatable("gui." + MODID + ".render_screen.button.render.region_too_large");
    private static final Text RENDER_SUCCESS_MSG =
            Text.translatable("gui." + MODID + ".render_screen.button.render.success");
    private static final Text RENDER_ERROR_MSG =
            Text.translatable("gui." + MODID + ".render_screen.button.render.error");

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

    private TextFieldWidget editbox1, editbox2;

    public RenderScreen() {
        super(ScreenTexts.EMPTY);

        this.bgWidth = 195;
        this.bgHeight = 136;
    }

    /**
     * Tells, if game should be paused.
     * @return {@code false}
     */
    @Override
    public boolean shouldPause() {
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
        deselectedTabs.add(addDrawableChild(new RenderScreenTab(bgPosLeft, bgPosTop - 28,
                TAB_TEXTURES[0], TAB_TEXTURES[1], TAB_TITLES[0],
                this::renderPRR, this::onTabPressed, new ItemStack(Items.PLAYER_HEAD),
                RenderScreenTab.Type.PLAYER_RELATIVE_RENDER)));
        deselectedTabs.add(addDrawableChild(new RenderScreenTab(bgPosLeft + 27, bgPosTop - 28,
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
        editbox1 = addDrawableChild(new TextFieldWidget(this.textRenderer,bgPosLeft + 8, bgPosTop + 32,
                179, 16, Text.literal("editbox1")));
        editbox1.setTooltip(Tooltip.of(EDITBOX_TOOLTIP));
        editbox1.setMaxLength(29);
        editbox2 = addDrawableChild(new TextFieldWidget(this.textRenderer,bgPosLeft + 8, bgPosTop + 67,
                179, 16, Text.literal("editbox2")));
        editbox2.setTooltip(Tooltip.of(EDITBOX_TOOLTIP));
        editbox2.setMaxLength(29);

        // Render button
        renderButton = addDrawableChild(new RenderButton(bgPosLeft + bgWidth / 2 - 30,
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
    public void render(@NotNull DrawContext drawContext, int mouseX, int mouseY, float partialTicks){
        this.renderBackground(drawContext);

        // Render all unselected tabs under background texture
        for(RenderScreenTab tab : this.deselectedTabs){
            tab.render(drawContext, mouseX, mouseY, partialTicks);
        }

        // Background texture
        drawContext.drawTexture(BACKGROUND_TEXTURE, bgPosLeft, bgPosTop,0, 0,
                bgWidth, bgHeight, bgWidth, bgHeight);

        // Render selected tab
        selectedTab.render(drawContext, mouseX, mouseY, partialTicks);

        // Render current tab contents
        selectedTab.renderMethod.render(drawContext, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders player relative render tab.
     * @param drawContext the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderPRR(@NotNull DrawContext drawContext, int mouseX, int mouseY, float partialTicks){
        // Tab title
        drawContext.drawText(this.textRenderer, TAB_TITLES[0], bgPosLeft + 8, bgPosTop + 6,
                0x404040, false);

        // Editbox 1
        drawContext.drawText(this.textRenderer, EDITBOX_TITLES[0], bgPosLeft + 12, bgPosTop + 19,
                0x404040, false);
        editbox1.render(drawContext, mouseX, mouseY, partialTicks);

        //Editbox 2
        drawContext.drawText(this.textRenderer, EDITBOX_TITLES[1], bgPosLeft + 12, bgPosTop + 54,
                0x404040, false);
        editbox2.render(drawContext, mouseX, mouseY, partialTicks);

        renderButton.render(drawContext, mouseX, mouseY, partialTicks);
    }

    /**
     * Renders absolute position render tab.
     * @param drawContext the GuiGraphics object used for rendering.
     * @param mouseX the x-coordinate of the mouse cursor.
     * @param mouseY the y-coordinate of the mouse cursor.
     * @param partialTicks the partial tick time.
     */
    public void renderAPR(@NotNull DrawContext drawContext, int mouseX, int mouseY, float partialTicks){
        // Tab title
        drawContext.drawText(this.textRenderer, TAB_TITLES[1], bgPosLeft + 8, bgPosTop + 6,
                0x404040, false);

        // Editbox 1
        drawContext.drawText(this.textRenderer, EDITBOX_TITLES[2], bgPosLeft + 12, bgPosTop + 19,
                0x404040, false);
        editbox1.render(drawContext, mouseX, mouseY, partialTicks);

        //Editbox 2
        drawContext.drawText(this.textRenderer, EDITBOX_TITLES[3], bgPosLeft + 12, bgPosTop + 54,
                0x404040, false);
        editbox2.render(drawContext, mouseX, mouseY, partialTicks);

        renderButton.render(drawContext, mouseX, mouseY, partialTicks);
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
    private void onRenderButtonPressed(PressableWidget button){
        // Safely get minecraft player
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {throw new UnsupportedOperationException("Player is null");}

        // Load input
        int x1, y1, z1, x2, y2, z2;
        try{
            // Editbox1
            StringReader r = new StringReader(editbox1.getText());
            x1 = r.readInt();
            r.skipWhitespace();
            y1 = r.readInt();
            r.skipWhitespace();
            z1 = r.readInt();

            // Editbox2
            r = new StringReader(editbox2.getText());
            x2 = r.readInt();
            r.skipWhitespace();
            y2 = r.readInt();
            r.skipWhitespace();
            z2 = r.readInt();
        }
        catch (Exception e){
            player.sendMessage(RENDER_WRONG_INPUT_MSG);
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
                player.sendMessage(RENDER_REGION_TOO_LARGE_MSG);
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
            renderRegion(player.getWorld(), fileWriters, posMin, posMax);

            // Notify about success
            player.sendMessage(RENDER_SUCCESS_MSG);

        }
        catch(Exception e) {
            LOGGER.error("RenderCube encountered error while rendering", e);
            player.sendMessage(RENDER_ERROR_MSG);
        }
    }
}
