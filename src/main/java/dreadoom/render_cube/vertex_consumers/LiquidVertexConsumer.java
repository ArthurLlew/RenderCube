package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Used to consume geometry data, produced by liquid renderers.
 */
public class LiquidVertexConsumer extends CommonVertexConsumer {
    /**
     * Liquid position in level.
     */
    private final BlockPos levelPosition;

    /**
     * Constructs instance from position in region.
     * @param regionPosition liquid position in region
     */
    public LiquidVertexConsumer(BlockPos regionPosition, BlockPos levelPosition){
        super(regionPosition);

        this.levelPosition = levelPosition;
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
                x - (levelPosition.getX() & 15),
                y - (levelPosition.getY() & 15),
                z - (levelPosition.getZ() & 15));
    }
}
