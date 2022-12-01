package com.shermobeaver.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shermobeaver.render_cube.rendered_entities.RenderedVertex;
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

    /**
     * Consumes vertex.
     */
    @Override
    public void vertex(float x,
                       float y,
                       float z,
                       float r,
                       float g,
                       float b,
                       float a,
                       float u,
                       float v,
                       int overlayCords,
                       int uv2,
                       float normal_x,
                       float normal_y,
                       float normal_z){
        vertices.add(new RenderedVertex(
                x - (position.getX() & 15),
                y - (position.getY() & 15),
                z - (position.getZ() & 15),
                u,
                v));
    }

    /**
     * Consumes vertex coordinates.
     */
    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z){
        return super.vertex(x - (position.getX() & 15), y - (position.getY() & 15), z - (position.getZ() & 15));
    }
}
