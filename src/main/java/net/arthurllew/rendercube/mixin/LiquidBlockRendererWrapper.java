package net.arthurllew.rendercube.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Modifies {@link LiquidBlockRenderer} behaviour.
 */
@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererWrapper {
    /**
     * Wraps shouldRenderFace in {@link LiquidBlockRenderer}. Allows to render faces on borders of render region.
     */
    @WrapMethod(method = "shouldRenderFace(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/block/state/BlockState;)Z")
    private static boolean wrapShouldRenderFace(BlockAndTintGetter level,
                                                BlockPos pos,
                                                FluidState fluidState,
                                                BlockState selfState,
                                                Direction dir,
                                                BlockState otherState,
                                                Operation<Boolean> original) {
        // Must be rendering
        if (RegionRenderer.STATE.isRendering()) {
            // 1) Block position in quad direction is outside the render region
            // (quad is on the render region border and is facing out of the render region)
            // 2) Quad direction must allow culling suppression
            if (RegionRenderer.STATE.isOutsideRenderRegion(pos.relative(dir))
                    && Config.DATA.regionBorderUnculledDirections.get(dir)) {
                return true;
            }
        }
        // Default behaviour
        return original.call(level, pos, fluidState, selfState, dir, otherState);
    }

    /**
     * Wraps isFaceOccludedByNeighbor in {@link LiquidBlockRenderer}.
     * Allows to render faces on borders of render region.
     */
    @WrapMethod(method = "isFaceOccludedByNeighbor")
    private static boolean wrapIsFaceOccludedByNeighbor(BlockGetter level, BlockPos pos, Direction side,
                                                        float height, BlockState blockState,
                                                        Operation<Boolean> original) {
        // Must be rendering
        if (RegionRenderer.STATE.isRendering()
                // 1) Quad direction must allow culling suppression
                // 2) Block position in quad direction is outside the render region
                // (quad is on the render region border and is facing out of the render region)
                && RegionRenderer.STATE.isOutsideRenderRegion(pos.relative(side))
                && Config.DATA.regionBorderUnculledDirections.getOrDefault(side, false)) {
            return false;
        }
        // Default behaviour
        return original.call(level, pos, side, height, blockState);
    }
}
