package com.render_cube.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RenderScreenTab {
    public final RenderScreenTabType type;
    public final RenderMethod renderMethod;
    public final int spritePosLeft, spritePosTop;
    public final ResourceLocation Textures;

    RenderScreenTab(RenderScreenTabType type, int spritePosLeft, int spritePosTop, ResourceLocation Textures,
                    RenderMethod renderMethod){
        this.type = type;
        this.spritePosLeft = spritePosLeft;
        this.spritePosTop = spritePosTop;
        this.Textures = Textures;
        this.renderMethod = renderMethod;
    }

    public boolean checkClicked(double mouseX, double mouseY) {
        return mouseX >= (double) spritePosLeft &&
                mouseX <= (double)(spritePosLeft + 26) &&
                mouseY >= (double)spritePosTop &&
                mouseY <= (double)(spritePosTop + 32);
    }

    @OnlyIn(Dist.CLIENT)
    public interface RenderMethod {
        void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks);
    }
}
