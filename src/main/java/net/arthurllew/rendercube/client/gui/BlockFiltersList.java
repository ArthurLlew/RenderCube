package net.arthurllew.rendercube.client.gui;

import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.arthurllew.rendercube.RenderCube.MODID;

public class BlockFiltersList extends ContainerObjectSelectionList<BlockFiltersList.Row> {
    /**
     * Block consumer settings culling rule text.
     */
    protected static final Component CULLING_RULE_BUTTON_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.blockfilters.button.culling");
    /**
     * Block consumer settings entry text.
     */
    protected static final Component ENTRY_EDITBOX_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.blockfilters.editbox.entry");
    /**
     * Block consumer settings filename text.
     */
    protected static final Component FILENAME_EDITBOX_TEXT =
            Component.translatable("gui." + MODID + ".screen.config.blockfilters.editbox.filename");

    /**
     * Constructor.
     * @param mc Minecraft
     * @param font screen font
     * @param w widget width
     * @param h widget height
     * @param y widget Y position
     */
    BlockFiltersList(Minecraft mc, Font font, Config.Data configDraft, int w, int h, int y) {
        super(mc, w, h, y, 60);

        // Construct block consumer settings list
        for (Config.Data.BlockConsumerConfig blockFilter : configDraft.blockConsumerConfigs) {
            addEntry(new BlockConsumerRow(font, blockFilter));
        }
    }

    /**
     * Generalized Vanilla method.
     * @return corrected row width
     */
    @Override
    public int getRowWidth() {
        // 6 px scrollbar + margins
        return this.width - 16;
    }

    /**
     * Generalized Vanilla method.
     * @return corrected scrollbar position
     */
    @Override
    protected int getScrollbarPosition() {
        // Moves scrollbar where it belongs
        return this.getX() + this.width - 6;
    }

    /**
     * List row.
     */
    abstract static class Row extends ContainerObjectSelectionList.Entry<Row> {}

    /**
     * Block consumer config entry.
     */
    static class BlockConsumerRow extends Row {
        /**
         * Type button.
         */
        private final CycleButton<Config.Data.BlockConsumerSettingsType> typeButton;
        /**
         * Culling rule button
         */
        private final CycleButton<Boolean> cullingRuleButton;
        /**
         * Editbox for block consumer settings entry.
         */
        protected EditBox entryEditbox;
        /**
         * Editbox for block consumer filename.
         */
        protected EditBox filenameEditbox;

        /**
         * Constructor.
         * @param blockConsumerConfig settings for block consumer
         */
        BlockConsumerRow(Font font, Config.Data.BlockConsumerConfig blockConsumerConfig) {
            // Type button
            this.typeButton = CycleButton.
                     <Config.Data.BlockConsumerSettingsType>builder(t -> Component.literal(t.name()))
                            .withValues(Config.Data.BlockConsumerSettingsType.values())
                            .withInitialValue(blockConsumerConfig.type)
                            .create(0, 0, 100, 20, Component.literal("Type"),
                                    (b, v)
                                            -> blockConsumerConfig.type = v);

            // Culling rule button
            this.cullingRuleButton = CycleButton.onOffBuilder(blockConsumerConfig.cullSides)
                    .create(0, 0, 60, 20, CULLING_RULE_BUTTON_TEXT,
                            (b, v) -> blockConsumerConfig.cullSides = v);

            // Editbox for block consumer settings entry
            this.entryEditbox = new EditBox(font, 0, 0, 170, 16, ENTRY_EDITBOX_TEXT);
            this.entryEditbox.setMaxLength(150);
            this.entryEditbox.setValue(blockConsumerConfig.entry);
            this.entryEditbox.setTooltip(Tooltip.create(ENTRY_EDITBOX_TEXT));
            this.entryEditbox.setResponder((v -> blockConsumerConfig.entry = v));

            // Editbox for block consumer filename
            this.filenameEditbox = new EditBox(font, 0, 0, 170, 16, FILENAME_EDITBOX_TEXT);
            this.filenameEditbox.setMaxLength(150);
            this.filenameEditbox.setValue(blockConsumerConfig.filename);
            this.filenameEditbox.setTooltip(Tooltip.create(FILENAME_EDITBOX_TEXT));
            this.filenameEditbox.setResponder((v -> blockConsumerConfig.filename = v));
        }

        /**
         * Renders row entry.
         */
        @Override
        public void render(@NotNull GuiGraphics guiGraphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Type button
            this.typeButton.setPosition(left - 6, top);
            this.typeButton.render(guiGraphics, mouseX, mouseY, partialTick);

            // Culling rule button
            this.cullingRuleButton.setPosition(left + 104, top);
            this.cullingRuleButton.render(guiGraphics, mouseX, mouseY, partialTick);

            // Editbox for block consumer settings entry
            this.entryEditbox.setPosition(left - 6, top + 22);
            this.entryEditbox.render(guiGraphics, mouseX, mouseY, partialTick);

            // Editbox for block consumer filename
            this.filenameEditbox.setPosition(left - 6, top + 40);
            this.filenameEditbox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        /**
         * Allows to interact with child widgets.
         */
        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(typeButton, cullingRuleButton, entryEditbox, filenameEditbox);
        }

        /**
         * Allows to narrate child widgets.
         */
        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(typeButton, cullingRuleButton, entryEditbox, filenameEditbox);
        }

        /**
         * Handles mouse click.
         */
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            // Try the default handling
            boolean handled = super.mouseClicked(mouseX, mouseY, button);
            // If handled by button (includes children)
            if (handled && this.getFocused() instanceof AbstractButton) {
                // Clear focus from any clicked child
                this.setFocused(null);
            }
            // Return result
            return handled;
        }
    }
}
