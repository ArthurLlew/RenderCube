package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Used to consume geometry data, produced by liquid renderers.
 */
public class LiquidVertexConsumer extends BasicVertexConsumer {
    /**
     * Liquid position in world.
     */
    private final BlockPos position;

    /**
     * Constructs instance from liquid position in world.
     * @param inPosition liquid position in world
     */
    public LiquidVertexConsumer(BlockPos inPosition){
        position = inPosition;
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
        return super.vertex(x - (position.getX() & 15), y - (position.getY() & 15), z - (position.getZ() & 15));
    }
}
