package net.arthurllew.rendercube.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
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
     * Wraps {@link Block#shouldRenderFace}. Allows to render faces on boarders of render region.
     */
    @WrapMethod(method = "shouldRenderFace")
    private static boolean wrapShouldRenderFace(BlockState state,
                                                BlockGetter level,
                                                BlockPos offset,
                                                Direction face,
                                                BlockPos pos,
                                                Operation<Boolean> original) {
        // Must be rendering and allowed by settings
        if (RegionRenderer.STATE.isRendering() && RegionRenderer.STATE.renderRegionBoarderFaceCulling()) {
            // If position toward direction is outside of render region
            if (RegionRenderer.STATE.isOutsideRenderRegion(pos.relative(face))) {
                return true;
            }
        }
        // Default behaviour
        return original.call(state, level, offset, face, pos);
    }
}
