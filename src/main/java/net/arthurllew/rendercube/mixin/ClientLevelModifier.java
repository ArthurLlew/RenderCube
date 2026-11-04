package net.arthurllew.rendercube.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Modifies {@link ClientLevel} behaviour.
 */
@Mixin(ClientLevel.class)
public class ClientLevelModifier {
    /**
     * Modifies {@link ClientLevel#getShade(Direction, boolean)}. Allows to control
     * capture of Minecraft's directional lighting.
     */
    @ModifyReturnValue(method = "getShade(Lnet/minecraft/core/Direction;Z)F", at = @At("RETURN"))
    private float noDirectionalShade(float original) {
        // Must be rendering
        if (RegionRenderer.STATE.isRendering()) {
            // Must be allowed by config
            if (!Config.DATA.useDirectionalShading) {
                return 1.0F;
            }
        }
        return original;
    }
}
