package net.arthurllew.rendercube.client.rendering;

import net.arthurllew.rendercube.client.io.DataWriters;
import net.arthurllew.rendercube.client.rendering.chunk.ChunkRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;

public abstract class RegionRenderer {
    /**
     * Renderer state.
     */
    public static final State STATE = new State();

    /**
     * Captures geometry of world region.
     * @param chunkRenderer                  chunk renderer implementation
     * @param regionMin                      min block position of the region to capture
     * @param regionMax                      max block position of the region to capture
     * @param perChunkRendering              whether to write data in unified files or split data files per chunk
     * @param renderRegionBoarderFaceCulling whether to not cull quads facing out of render region and
     *                                       located on its boarder
     **/
    public static void captureRegion(@NotNull ChunkRenderer chunkRenderer,
                                     @NotNull BlockPos regionMin, @NotNull BlockPos regionMax,
                                     boolean perChunkRendering,
                                     boolean renderRegionBoarderFaceCulling) throws IOException {
        // Only render if not rendering
        if (!STATE.isRendering()) {
            try {
                // Init renderer
                STATE.chunkRenderer = chunkRenderer;
                STATE.setRegionPositions(regionMin, regionMax);
                STATE.renderRegionBoarderFaceCulling = renderRegionBoarderFaceCulling;
                // Date time string
                String dateTimeStr = LocalDateTime.now().toString()
                        .replace("T", "_").replace(":", "-");
                STATE.dateTime = dateTimeStr.substring(0, dateTimeStr.lastIndexOf("."));

                // Capture region with per-chunk data writers
                if (perChunkRendering) {
                    captureRegion(null);
                }
                // Capture region with unified data writers
                else {
                    try (DataWriters dataWriters = new DataWriters(STATE.dateTime, (name) -> name)) {
                        captureRegion(dataWriters);
                    }
                }
            }
            // Stop renderer
            finally {
                STATE.chunkRenderer = null;
            }
        }
    }

    /**
     * Captures geometry of world region.
     * @param dataWriters used to write captured data
     **/
    protected static void captureRegion(@Nullable DataWriters dataWriters) throws IOException {
        // Loop over chunks in region
        for(int chunkX = SectionPos.blockToSectionCoord(STATE.regionMin.getX());
            chunkX <= SectionPos.blockToSectionCoord(STATE.regionMax.getX());
            chunkX++){
            for(int chunkZ = SectionPos.blockToSectionCoord(STATE.regionMin.getZ());
                chunkZ <= SectionPos.blockToSectionCoord(STATE.regionMax.getZ());
                chunkZ++){
                // Capture chunk with unified data writers
                if (dataWriters != null) {
                    STATE.chunkRenderer.captureChunk(dataWriters, STATE.regionMin, STATE.regionMax,
                            new ChunkPos(chunkX, chunkZ));
                }
                // Capture chunk with per-chunk data writers
                else {
                    // Lambda vars should be final
                    final int fChunkX = chunkX;
                    final int fChunkZ = chunkZ;
                    // Files for each chunk will have their coordinates as prefix
                    try(DataWriters chunkDataWriters = new DataWriters(STATE.dateTime,
                            (name) -> fChunkX + "." + fChunkZ + "-" + name)) {
                        // Capture chunk
                        STATE.chunkRenderer.captureChunk(chunkDataWriters, STATE.regionMin, STATE.regionMax,
                                new ChunkPos(chunkX, chunkZ));
                    }
                }
            }
        }
    }

    /**
     * Renderer state.
     */
    public static class State {
        /**
         * Chunk renderer.
         */
        ChunkRenderer chunkRenderer = null;

        /**
         * Datetime of rendering.
         */
        protected String dateTime = null;

        // Region positions
        protected BlockPos regionMin, regionMax = BlockPos.ZERO;

        /**
         * Whether to not cull quads facing out of render region and located on its boarder.
         */
        protected boolean renderRegionBoarderFaceCulling = false;

        /**
         * @return whether any rendering is in process
         */
        public boolean isRendering() {
            return chunkRenderer != null;
        }

        /**
         * @return whether to not cull quads facing out of render region and located on its boarder
         */
        public boolean renderRegionBoarderFaceCulling() {
            return renderRegionBoarderFaceCulling;
        }

        /**
         * Sets region positions.
         */
        protected void setRegionPositions(@NotNull BlockPos regionMin, @NotNull BlockPos regionMax) {
            this.regionMin = regionMin;
            this.regionMax = regionMax;
        }

        /**
         * @return whether provided position is outside render region
         */
        public boolean isOutsideRenderRegion(@NotNull BlockPos pos) {
            return pos.getX() < this.regionMin.getX()
                    || pos.getY() < this.regionMin.getY()
                    || pos.getZ() < this.regionMin.getZ()
                    || pos.getX() > this.regionMax.getX()
                    || pos.getY() > this.regionMax.getY()
                    || pos.getZ() > this.regionMax.getZ();
        }
    }
}