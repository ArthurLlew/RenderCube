package net.arthurllew.rendercube.client.rendering.chunk;

import net.arthurllew.rendercube.client.io.DataWriters;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class ChunkRenderer {
    /**
     * Captures geometry of one chunk.
     * @param dataWriters used to write captured data
     * @param regionMin   min block position of the region to capture
     * @param regionMax   max block position of the region to capture
     * @param chunkPos    position of chunk being rendered
     **/
    public abstract void captureChunk(@NotNull DataWriters dataWriters,
                                      @NotNull BlockPos regionMin, @NotNull BlockPos regionMax,
                                      @NotNull ChunkPos chunkPos) throws IOException;
}
