package net.arthurllew.rendercube.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.arthurllew.rendercube.client.rendering.CubesRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Modifies {@link LiquidBlockRenderer} behaviour.
 */
@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererWrapper {
    /**
     * Wraps isFaceOccludedByState in {@link LiquidBlockRenderer}. Allows to render faces on boarders of render region.
     */
    @WrapMethod(method = "isFaceOccludedByState")
    private static boolean wrapShouldRenderFace(BlockGetter level,
                                                Direction face,
                                                float height,
                                                BlockPos pos,
                                                BlockState state,
                                                Operation<Boolean> original) {
        // Must be rendering and allowed by settings
        if (CubesRenderer.State.INSTANCE.isRendering && CubesRenderer.State.INSTANCE.noRenderRegionBoarderFaceCulling) {
            // If position toward direction is outside of render region
            if (CubesRenderer.State.INSTANCE.isOutsideRenderRegion(pos.relative(face))) {
                return true;
            }
        }
        // Default behaviour
        return original.call(level, face, height, pos, state);
    }
}
