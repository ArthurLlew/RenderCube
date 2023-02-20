package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Used to consume geometry data, produced by liquid renderers.
 */
public class LiquidVertexConsumer extends BasicVertexConsumer {
    /**
     * Constructs instance from position in region.
     * @param regionPosition liquid position in region
     */
    public LiquidVertexConsumer(BlockPos regionPosition){
        super(regionPosition);
    }

    /**
     * Saves vertex coordinates, adjusted by magical value used, when liquid is rendered.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z){
        return super.vertex(
                x - (regionPosition.getX() & 15),
                y - (regionPosition.getY() & 15),
                z - (regionPosition.getZ() & 15));
    }
}
