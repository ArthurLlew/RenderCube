package com.rendercube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * Used to consume geometry data, produced by renderers.
 */
public class CommonVertexConsumer extends BasicVertexConsumer {
    /**
     * Position in region.
     */
    private final BlockPos regionPos;

    /**
     * Constructs instance from position in region.
     * @param fileStream opened file output stream where data will be saved
     * @param regionPos liquid position in region
     */
    public CommonVertexConsumer(OutputStream fileStream, BlockPos regionPos) {
        super(fileStream);

        this.regionPos = regionPos;
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
        super.vertex(regionPos.getX() + x, regionPos.getY() + y, regionPos.getZ() + z);
        return this;
    }
}
