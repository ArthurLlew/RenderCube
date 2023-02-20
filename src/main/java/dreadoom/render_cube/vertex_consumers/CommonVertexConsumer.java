package dreadoom.render_cube.vertex_consumers;

import net.minecraft.core.BlockPos;

/**
 * Used to consume geometry data, produced by renderers.
 */
public class CommonVertexConsumer extends BasicVertexConsumer {
    /**
     * Constructs instance from position in region.
     * @param regionPosition liquid position in region
     */
    public CommonVertexConsumer(BlockPos regionPosition){
        super(regionPosition);
    }
}
