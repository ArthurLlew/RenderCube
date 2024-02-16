package com.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * Used to consume geometry data, produced by liquid renderers.
 */
public class LiquidVertexConsumer extends CommonVertexConsumer {
    /**
     * Constructs instance from position in region.
     * @param regionPosition liquid position in region
     */
    public LiquidVertexConsumer(OutputStream fileStream, BlockPos regionPosition) {
        super(fileStream, regionPosition);
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
        // Rendering process of liquid adds a block position in a chunk to vertex coordinate
        int shiftX = ((int)Math.ceil(x) >> 1) << 1;
        int shiftY = ((int)Math.ceil(y) >> 1) << 1;
        int shiftZ = ((int)Math.ceil(z) >> 1) << 1;

        return super.vertex(
                x - shiftX,
                y - shiftY,
                z - shiftZ);
    }
}
