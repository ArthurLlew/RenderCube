package net.arthurllew.rendercube.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Modifies {@link LiquidBlockRenderer} behaviour.
 */
@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererWrapper {
    /**
     * Wraps shouldRenderFace in {@link LiquidBlockRenderer}. Allows to render faces on boarders of render region.
     */
    @WrapMethod(method = "shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    private static boolean wrapShouldRenderFace(BlockAndTintGetter level,
                                                BlockPos pos,
                                                FluidState fluidState,
                                                BlockState selfState,
                                                Direction direction,
                                                BlockState otherState,
                                                Operation<Boolean> original) {
        // Must be rendering and allowed by settings
        if (RegionRenderer.STATE.isRendering() && RegionRenderer.STATE.noRenderRegionBoarderFaceCulling()) {
            // If position toward direction is outside of render region
            if (RegionRenderer.STATE.isOutsideRenderRegion(pos.relative(direction))) {
                return true;
            }
        }
        // Default behaviour
        return original.call(level, pos, fluidState, selfState, direction, otherState);
    }
}
