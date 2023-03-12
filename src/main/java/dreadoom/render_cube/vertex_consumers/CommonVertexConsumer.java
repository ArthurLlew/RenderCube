package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Used to consume geometry data, produced by renderers.
 */
public class CommonVertexConsumer extends BasicVertexConsumer {
    /**
     * Position in region.
     */
    private final BlockPos regionPosition;

    /**
     * Constructs instance from position in region.
     * @param regionPosition liquid position in region
     */
    public CommonVertexConsumer(BlockPos regionPosition){
        this.regionPosition = regionPosition;
    }

    /**
     * Saves vertex coordinates adjusted by position in region.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z){
        super.vertex(regionPosition.getX() + x, regionPosition.getY() + y, regionPosition.getZ() + z);
        return this;
    }
}
