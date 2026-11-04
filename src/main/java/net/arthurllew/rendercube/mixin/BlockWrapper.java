package net.arthurllew.rendercube.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Modifies {@link Block} behaviour.
 */
@Mixin(Block.class)
public abstract class BlockWrapper {
    /**
     * Wraps {@link Block#shouldRenderFace}. Allows to render faces on borders of render region.
     */
    @WrapMethod(method = "shouldRenderFace")
    private static boolean wrapShouldRenderFace(BlockState state,
                                                BlockGetter level,
                                                BlockPos offset,
                                                Direction dir,
                                                BlockPos neighborPos,
                                                Operation<Boolean> original) {
        // Must be rendering
        if (RegionRenderer.STATE.isRendering()) {
            // 1) Block position in quad direction is outside the render region
            // (quad is on the render region border and is facing out of the render region)
            // 2) Quad direction must allow culling suppression
            if (RegionRenderer.STATE.isOutsideRenderRegion(neighborPos)
                    && Config.DATA.regionBorderUnculledDirections.get(dir)) {
                return true;
            }
        }
        // Default behaviour
        return original.call(state, level, offset, dir, neighborPos);
    }
}
