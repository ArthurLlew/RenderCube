package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dreadoom.render_cube.rendered_entities.RenderedVertex;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z){
        return super.vertex(x - (position.getX() & 15), y - (position.getY() & 15), z - (position.getZ() & 15));
    }
}
