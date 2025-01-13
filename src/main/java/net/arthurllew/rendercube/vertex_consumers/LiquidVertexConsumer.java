package net.arthurllew.rendercube.vertex_consumers;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * Used to consume geometry data, produced by liquid renderers.
 */
public class LiquidVertexConsumer extends CommonVertexConsumer {
    /**
     * Position in level.
     */
    private final BlockPos levelPos;

    /**
     * Constructs instance from position in region.
     * @param fileStream opened file output stream where data will be saved
     * @param regionPos liquid position in region
     * @param levelPos liquid position in level
     */
    public LiquidVertexConsumer(OutputStream fileStream, BlockPos regionPos, BlockPos levelPos) {
        super(fileStream, regionPos);

        this.levelPos = levelPos;
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
        // Rendering process of liquid adds a block position in a chunk to vertex coordinate:
        // vertex position + (level position & 15)
        // This operation puts liquid into a chunk space.
        // In some mods, like Optifine value 15 is changed to 255 (bigger chunks or whatever).
        // So, the first part of shift takes care of Vanilla version tweaks and the second fixes Optifine tweaks
        // (moves given point to chunk of size 16x16, so Vanilla shifting will work properly).
        int chunkPosX = levelPos.getX() & 15;
        int chunkPosY = levelPos.getY() & 15;
        int chunkPosZ = levelPos.getZ() & 15;
        int shiftX = chunkPosX + (((int)Math.floor(x) - chunkPosX) & 240);
        int shiftY = chunkPosY + (((int)Math.floor(y) - chunkPosY) & 240);
        int shiftZ = chunkPosZ + (((int)Math.floor(z) - chunkPosZ) & 240);

        return super.vertex(x - shiftX, y - shiftY, z - shiftZ);
    }
}
