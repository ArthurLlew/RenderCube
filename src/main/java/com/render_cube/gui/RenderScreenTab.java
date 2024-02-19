package com.render_cube.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class RenderScreenTab {
    public final RenderMethod renderMethod;

    public RenderScreenTab(RenderMethod renderMethod){
        this.renderMethod = renderMethod;
    }

    @OnlyIn(Dist.CLIENT)
    public interface RenderMethod {
        void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
    }
}
