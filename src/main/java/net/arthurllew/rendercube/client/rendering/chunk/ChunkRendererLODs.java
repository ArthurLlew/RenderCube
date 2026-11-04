package net.arthurllew.rendercube.client.rendering.chunk;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.arthurllew.rendercube.client.io.DataWriters;
import net.arthurllew.rendercube.client.rendering.vertex.CommonVertexConsumer;
import net.arthurllew.rendercube.client.texture.LODsColorResolver;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class ChunkRendererLODs extends ChunkRenderer implements AutoCloseable {
    /**
     * LODs columns cache capacity.
     */
    protected final int COLUMN_CACHE_CAPACITY = 32;

    /**
     * LODs quads have default UVs.
     */
    protected static final float[] DEFAULT_UV = { 0f, 0f,  1f, 0f,  1f, 1f,  0f, 1f };

    // Normals for different faces
    protected static final float[] NORMAL_UP    = {  0f,  1f,  0f };
    protected static final float[] NORMAL_DOWN  = {  0f, -1f,  0f };
    protected static final float[] NORMAL_NORTH = {  0f,  0f, -1f };
    protected static final float[] NORMAL_SOUTH = {  0f,  0f,  1f };
    protected static final float[] NORMAL_EAST  = {  1f,  0f,  0f };
    protected static final float[] NORMAL_WEST  = { -1f,  0f,  0f };

    /**
     * Level accessor.
     */
    protected final Level level;

    /**
     * LODs column XZ size.
     */
    protected final int columnWidth;

    /**
     * Custom resolver of block colors.
     */
    protected final LODsColorResolver colorResolver = new LODsColorResolver();

    /**
     * LODs columns cache.
     */
    Long2ObjectLinkedOpenHashMap<LODsColumnElement[]> columnsCache =
            new Long2ObjectLinkedOpenHashMap<>(COLUMN_CACHE_CAPACITY);

    /**
     * Constructor.
     * @param level         level accessor
     * @param levelOfDetail LODs level
     */
    public ChunkRendererLODs(Level level, byte levelOfDetail) {
        // Check detail level
        if (levelOfDetail < 0 || levelOfDetail > 4) {
            throw new IllegalArgumentException("LODs level must be from 0 to 4 within a single chunk, but got "
                    + levelOfDetail);
        }

        this.level = level;
        this.columnWidth = 1 << levelOfDetail;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void captureChunk(@NotNull DataWriters dataWriters,
                             @NotNull BlockPos regionMin, @NotNull BlockPos regionMax,
                             @NotNull ChunkPos chunkPos) throws IOException {
        // Current chunk world relative coordinates
        int originX = SectionPos.sectionToBlockCoord(chunkPos.x);
        int originZ = SectionPos.sectionToBlockCoord(chunkPos.z);

        // Indexes of columns to render
        int minColumnIdxX = Mth.clamp(originX, regionMin.getX(), regionMax.getX());
        int minColumnIdxZ = Mth.clamp(originZ, regionMin.getZ(), regionMax.getZ());
        int maxColumnIdxX = Mth.clamp(originX + 15, regionMin.getX(), regionMax.getX());
        int maxColumnIdxZ = Mth.clamp(originZ + 15, regionMin.getZ(), regionMax.getZ());
        minColumnIdxX = SectionPos.sectionRelative(minColumnIdxX) / this.columnWidth;
        minColumnIdxZ = SectionPos.sectionRelative(minColumnIdxZ) / this.columnWidth;
        maxColumnIdxX = (SectionPos.sectionRelative(maxColumnIdxX) / this.columnWidth) + 1;
        maxColumnIdxZ = (SectionPos.sectionRelative(maxColumnIdxZ) / this.columnWidth) + 1;

        // Render region Y span (+ offset for culling purposes)
        int minY = regionMin.getY() - 1;
        int maxY = regionMax.getY() + 1;

        // Render chunk
        for (int iX = minColumnIdxX; iX < maxColumnIdxX; iX++) {
            for (int iZ = minColumnIdxZ; iZ < maxColumnIdxZ; iZ++) {
                // Column XZ world relative coordinates
                int x = originX + iX * this.columnWidth;
                int z = originZ + iZ * this.columnWidth;

                // Get or generate LODs column
                LODsColumnElement[] column = this.getLODsColumn(x, z, minY, maxY);

                // Render LODs column
                captureLODsColumn(dataWriters, column, x, z, regionMin, regionMax);
            }
        }
    }

    /**
     * Tries to find requested column in cache or generates a new one.
     * @param x    X block coordinate
     * @param z    Z block coordinate
     * @param minY min Y block coordinate
     * @param maxY max Y block coordinate
     * @return LODs column
     */
    protected LODsColumnElement[] getLODsColumn(int x, int z, int minY, int maxY) {
        LODsColumnElement[] column;

        // Convert pos to key
        long key = ChunkPos.asLong(x, z);

        // Try to get existing column or create a new one
        if ((column = this.columnsCache.get(key)) == null) {
            // Cache size must remain below capacity
            if (this.columnsCache.size() >= COLUMN_CACHE_CAPACITY) {
                this.columnsCache.removeFirst();
            }

            // Generate and cache a new column
            column = this.buildLODsColumn(x, z, minY, maxY, this.columnWidth);
            this.columnsCache.put(key, column);
        }
        return column;
    }

    /**
     * Builds LODs column.
     * @param x           X block coordinate
     * @param z           Z block coordinate
     * @param minY        min Y block coordinate
     * @param maxY        max Y block coordinate
     * @param columnWidth LODs column XZ size
     * @return LODs column
     */
    protected LODsColumnElement[] buildLODsColumn(int x, int z, int minY, int maxY, int columnWidth) {
        // Column height in blocks
        int height = maxY - minY;

        // Sample all cells inside a column
        ColumnCell[] columnCells = new ColumnCell[height];
        for (int i = 0; i < height; i++) {
            columnCells[i] = sampleCell(x, minY + i, z, columnWidth);
        }

        // Init LODs column
        List<LODsColumnElement> column = new ArrayList<>();

        // Bottom element (used for culling) goes as is
        addColumnElement(column, x, z, minY, minY + 1, columnCells[0]);

        // Iterate column
        int prevIdx = 0;
        for (int i = 1; i < height - 1; i++) {
            // If current cell differs from previous or serves as offset
            if (!columnCells[i].isSame(columnCells[prevIdx])) {
                addColumnElement(column, x, z, minY + prevIdx, minY + i, columnCells[prevIdx]);
                prevIdx = i;
            }
        }
        // Add the last column data point
        addColumnElement(column, x, z, minY + prevIdx, maxY - 1, columnCells[prevIdx]);

        // Top element (used for culling)
        addColumnElement(column, x, z, maxY - 1, maxY, columnCells[height - 1]);

        // Convert to array and return
        return column.toArray(new LODsColumnElement[0]);
    }

    /**
     * Compressed LODs column cell contents. Blocks and biomes get majority vote (air does not vote).
     * Block light is a mean of total light hitting a cell.
     * @param x X block coordinate
     * @param y Y block coordinate
     * @param z Z block coordinate
     * @param columnWidth LODs column XZ size
     * @return sampled LODs column cell
     */
    protected ColumnCell sampleCell(int x, int y, int z, int columnWidth) {
        // All block states and biomes in this cell
        Map<BlockState, Integer> states = new HashMap<>();
        Map<Holder<Biome>, Integer> biomes = new HashMap<>();
        // Total block light in this cell
        int totalBlockLight = 0;
        // The amount of positions where light was sampled
        int blockLightSamples = 0;

        // Prepare mutable block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Iterate inside a box surrounding this cell
        for (int iX = -1; iX < columnWidth + 1; iX++) {
            for (int iY = -1; iY < 2; iY++) {
                for (int iZ = -1; iZ < columnWidth + 1; iZ++) {
                    // Light hitting the cell faces is combined from its inside and blocks touching it,
                    // so box edges and corners are skipped
                    if ((iX < 0 || iX >= columnWidth ? 1 : 0)
                            + (iY != 0 ? 1 : 0)
                            + (iZ < 0 || iZ >= columnWidth ? 1 : 0) > 1) {
                        continue;
                    }

                    // Calculate current position
                    pos.set(x + iX, y + iY, z + iZ);
                    // Get block state at it
                    BlockState block = this.level.getBlockState(pos);

                    // If position is inside a cell
                    if ((iZ > -1) && (iZ < columnWidth) && (iX > -1) && (iX < columnWidth) && (iY == 0)) {
                        // If block is not empty
                        if (!block.isAir()) {
                            // Add to vote
                            states.merge(block, 1, Integer::sum);
                            biomes.merge(this.level
                                            .getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2),
                                    1, Integer::sum);
                        }
                    }

                    // Get block light at this position
                    int blockLight = this.level.getBrightness(LightLayer.BLOCK, pos);
                    // Do not allow solid blocks to drag mean down
                    if (!(block.canOcclude() && blockLight == 0)) {
                        totalBlockLight += this.level.getBrightness(LightLayer.BLOCK, pos);
                        blockLightSamples++;
                    }
                }
            }
        }
        // Mean with rounding to the nearest (number of samples might equal 0)
        totalBlockLight = (blockLightSamples == 0) ? 0 : (totalBlockLight + blockLightSamples / 2) / blockLightSamples;

        // Finalize column cell
        return new ColumnCell(mostVoted(states), mostVoted(biomes), totalBlockLight);
    }

    /**
     * Performs majority vote over a map.
     * @param votes votes map
     * @return key with the most votes or {@code null} if nobody voted
     * @param <T> map key type
     */
    private static <T> @Nullable T mostVoted(Map<T, Integer> votes) {
        return votes.isEmpty() ? null : Collections.max(votes.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * Adds LODs column element.
     * @param column          chunk LODS columns list
     * @param x               X block coordinate
     * @param z               Z block coordinate
     * @param bottomYBlockPos LODS column element bottom Y block coordinate
     * @param topYBlockPos    LODS column element top Y block coordinate
     * @param cell            LODS column cell
     */
    protected void addColumnElement(@NotNull List<LODsColumnElement> column,
                                    int x, int z, int bottomYBlockPos, int topYBlockPos, ColumnCell cell) {
        // Skip empty cell
        if (cell.state == null || topYBlockPos <= bottomYBlockPos) return;

        // Position in the middle of column element
        BlockPos middlePos = new BlockPos(
                x + this.columnWidth/2,
                bottomYBlockPos + (topYBlockPos - bottomYBlockPos) / 2,
                z + this.columnWidth/2);

        // Add column element
        column.add(new LODsColumnElement(bottomYBlockPos, topYBlockPos, cell.state,
                this.colorResolver.getBlockColor(this.level, cell.state, middlePos),
                cell.blockLight));
    }

    /**
     * Renders LODs column with provided vertex consumer.
     * @param dataWriters used to write captured data
     * @param column      chunk LODs column
     * @param minX        LODs column X world position
     * @param minZ        LODs column Z world position
     * @param regionMin   min block position of the region to capture
     * @param regionMax   max block position of the region to capture
     */
    public void captureLODsColumn(@NotNull DataWriters dataWriters, LODsColumnElement[] column, int minX, int minZ,
                                  @NotNull BlockPos regionMin, @NotNull BlockPos regionMax) throws IOException {
        // Skip empty column
        if (column == null) {
            return;
        }

        // LODs column relative chunk position
        BlockPos regionPos = new BlockPos(0, -regionMin.getY(), 0);

        // World relative XZ span of the current column
        int maxX = minX + this.columnWidth;
        int maxZ = minZ + this.columnWidth;

        // Skip if the current column is not at least partially inside render region
        if (minX >= regionMax.getX() || maxX <= regionMin.getX()
                || minZ >= regionMax.getZ() || maxZ <= regionMin.getZ()) {
            return;
        }

        // XZ sizes of the ender region
        int regionSizeX = regionMax.getX() - regionMin.getX();
        int regionSizeZ = regionMax.getZ() - regionMin.getZ();

        // Column vertex XZ coordinates inside render region (clamped by render region box)
        float x0 = Mth.clamp(minX, regionMin.getX(), regionMax.getX()) - regionMin.getX();
        float x1 = Mth.clamp(maxX, regionMin.getX(), regionMax.getX()) - regionMin.getX();
        float z0 = Mth.clamp(minZ, regionMin.getZ(), regionMax.getZ()) - regionMin.getZ();
        float z1 = Mth.clamp(maxZ, regionMin.getZ(), regionMax.getZ()) - regionMin.getZ();

        // For every column element
        for (int i = 0; i < column.length; i++) {
            // Get element
            LODsColumnElement element = column[i];

            // This column element vertex consumer
            String filename = element.state().is(Blocks.WATER) ? "WaterLODs" : "BlocksLODs";
            VertexConsumer vertexConsumer = new CommonVertexConsumer(dataWriters.get(filename), regionPos);

            // World relative Y span of the current column element
            int minY = element.bottomYBlockPos();
            int maxY = element.topYBlockPos();

            // Skip if the current column element is not at least partially inside render region
            if (minY >= regionMax.getY() || maxY <= regionMin.getY()) {
                continue;
            }

            // Column vertex Y coordinates (clamped by render region box)
            float y0 = Mth.clamp(minY, regionMin.getY(), regionMax.getY());
            float y1 = Mth.clamp(maxY, regionMin.getY(), regionMax.getY());
            // Border culling flags
            boolean ignoreCullingN = (z0 == 0)
                    && Config.DATA.regionBorderUnculledDirections.get(Direction.NORTH);
            boolean ignoreCullingW = (x0 == 0)
                    && Config.DATA.regionBorderUnculledDirections.get(Direction.WEST);
            boolean ignoreCullingS = (z1 == regionSizeZ)
                    && Config.DATA.regionBorderUnculledDirections.get(Direction.SOUTH);
            boolean ignoreCullingE = (x1 == regionSizeX)
                    && Config.DATA.regionBorderUnculledDirections.get(Direction.EAST);
            boolean ignoreCullingU = (y1 == regionMax.getY())
                    && Config.DATA.regionBorderUnculledDirections.get(Direction.UP);
            boolean ignoreCullingD = (y0 == regionMin.getY())
                    && Config.DATA.regionBorderUnculledDirections.get(Direction.DOWN);

            // If bottom is not blocked or face is on region border and DOWN culling is off
            LODsColumnElement below = (i > 0) ? column[i - 1] : null;
            if (below == null || below.state().is(Blocks.WATER)
                    || below.topYBlockPos() < element.bottomYBlockPos()
                    || ignoreCullingD) {
                // Render DOWN face
                putHorizontalQuad(vertexConsumer, Direction.DOWN, element, x0, z0, x1, z1, y0);
            }

            // Render SIDE faces
            putVerticalSide(vertexConsumer, Direction.NORTH, element,
                    getNeighboringColumn(minX, minZ - this.columnWidth, regionMin, regionMax, ignoreCullingN),
                    x0, z0, x1, z1, y0, y1);
            putVerticalSide(vertexConsumer, Direction.WEST, element,
                    getNeighboringColumn(minX - this.columnWidth, minZ, regionMin, regionMax, ignoreCullingW),
                    x0, z0, x1, z1, y0, y1);
            putVerticalSide(vertexConsumer, Direction.SOUTH, element,
                    getNeighboringColumn(minX, minZ + this.columnWidth, regionMin, regionMax, ignoreCullingS),
                    x0, z0, x1, z1, y0, y1);
            putVerticalSide(vertexConsumer, Direction.EAST, element,
                    getNeighboringColumn(minX + this.columnWidth, minZ, regionMin, regionMax, ignoreCullingE),
                    x0, z0, x1, z1, y0, y1);

            // If top is not blocked or face is on region border and UP culling is off
            LODsColumnElement above = (i < column.length - 1) ? column[i + 1] : null;
            if (above == null || above.state().is(Blocks.WATER)
                    || above.bottomYBlockPos() > element.topYBlockPos()
                    || ignoreCullingU) {
                // Render UP face
                putHorizontalQuad(vertexConsumer, Direction.UP, element, x0, z0, x1, z1, y1);
            }
        }
    }

    /**
     * @param x             LODs column X world position
     * @param z             LODs column Z world position
     * @param regionMin     min block position of the region to capture
     * @param regionMax     max block position of the region to capture
     * @param ignoreCulling whether the culling is ignored
     * @return neighboring LODs column
     */
    protected LODsColumnElement[] getNeighboringColumn(int x, int z,
                                                       @NotNull BlockPos regionMin, @NotNull BlockPos regionMax,
                                                       boolean ignoreCulling) {
        // Return neighbor if culling is not ignored
        return ignoreCulling ? null : this.getLODsColumn(x, z, regionMin.getY() - 1, regionMax.getY() + 1);
    }

    /**
     * Renders horizontal LODs column side.
     * @param vertexConsumer    vertex consumer
     * @param face              LODs column side direction
     * @param element           LODs column element
     * @param neighboringColumn neighboring LODs column in LODs column side direction
     * @param x0                X0 coordinate
     * @param z0                Z0 coordinate
     * @param x1                X1 coordinate
     * @param z1                Z1 coordinate
     * @param y0                Y0 coordinate
     * @param y1                Y1 coordinate
     */
    protected static void putVerticalSide(VertexConsumer vertexConsumer, Direction face, LODsColumnElement element,
                                          LODsColumnElement[] neighboringColumn,
                                          float x0, float z0, float x1, float z1, float y0, float y1) {
        // Exposed areas of column (initialized as entire column)
        List<float[]> exposedAreas = new ArrayList<>();
        exposedAreas.add(new float[]{y0, y1});

        // If column has neighbor in required direction
        if (neighboringColumn != null) {
            // Whether element is water
            boolean elementIsWater = element.state().is(Blocks.WATER);

            // Iterate neighboring column and update exposed areas
            for (LODsColumnElement neighboringElement : neighboringColumn) {
                // If neighboring column element is not empty
                if (neighboringElement != null) {
                    // Whether neighboring element is water
                    boolean neighborIsWater = neighboringElement.state().is(Blocks.WATER);
                    // Water: hidden by water or by an occluding block.
                    // Block: hidden only by an occluding block.
                    if (elementIsWater
                            ? (neighborIsWater || neighboringElement.state().canOcclude())
                            : (!neighborIsWater && neighboringElement.state().canOcclude())) {
                        // Update hidden element parts
                        updateExposedAreas(exposedAreas,
                                neighboringElement.bottomYBlockPos(), neighboringElement.topYBlockPos());
                    }
                }
            }
        }

        // Render all exposed areas as vertical quads
        for (float[] exposedHeights : exposedAreas) {
            putVerticalQuad(vertexConsumer, face, element, x0, z0, x1, z1, exposedHeights[0], exposedHeights[1]);
        }
    }

    /**
     * Updates exposed areas depending on neighbor Y span.
     * @param exposedAreas    exposed areas
     * @param neighborBottomY neighbor bottom Y
     * @param neighborTopY    neighbor top Y
     */
    protected static void updateExposedAreas(List<float[]> exposedAreas, float neighborBottomY, float neighborTopY) {
        // Init result
        List<float[]> result = new ArrayList<>();
        // For all exposed areas
        for (float[] exposedArea : exposedAreas) {
            // If the neighbor is not intersecting with current area
            if (neighborTopY <= exposedArea[0] || exposedArea[1] <= neighborBottomY) {
                result.add(exposedArea);
            }
            // Otherwise
            else {
                // If the top part is not covered by the neighbor
                if (neighborTopY < exposedArea[1]) {
                    result.add(new float[]{neighborTopY, exposedArea[1]});
                }
                // If the bottom part is not covered by the neighbor
                if (exposedArea[0] < neighborBottomY) {
                    result.add(new float[]{exposedArea[0], neighborBottomY});
                }
            }
        }
        // Update exposed areas
        exposedAreas.clear();
        exposedAreas.addAll(result);
    }

    /**
     * Renders horizontal quad.
     * @param vertexConsumer vertex consumer
     * @param face           quad direction
     * @param element        LODs column element
     * @param x0             X0 coordinate
     * @param z0             Z0 coordinate
     * @param x1             X1 coordinate
     * @param z1             Z1 coordinate
     * @param y              Y coordinate
     */
    protected static void putHorizontalQuad(VertexConsumer vertexConsumer, Direction face, LODsColumnElement element,
                                            float x0, float z0, float x1, float z1, float y) {
        float[] coords = switch (face) {
            case UP ->   new float[]{ x0, y, z1,  x1, y, z1,  x1, y, z0,  x0, y, z0 };
            case DOWN -> new float[]{ x0, y, z0,  x1, y, z0,  x1, y, z1,  x0, y, z1 };
            default -> throw new IllegalArgumentException("Wrong direction for horizontal quad: " + face);
        };
        putQuad(vertexConsumer, face, element, coords);
    }

    /**
     * Renders horizontal quad.
     * @param vertexConsumer vertex consumer
     * @param face           quad direction
     * @param element        LODs column element
     * @param x0             X0 coordinate
     * @param z0             Z0 coordinate
     * @param x1             X1 coordinate
     * @param z1             Z1 coordinate
     * @param y0             Y0 coordinate
     * @param y1             Y1 coordinate
     */
    protected static void putVerticalQuad(VertexConsumer vertexConsumer, Direction face, LODsColumnElement element,
                                          float x0, float z0, float x1, float z1, float y0, float y1) {
        float[] coords = switch (face) {
            case NORTH -> new float[]{ x0, y0, z0,  x0, y1, z0,  x1, y1, z0,  x1, y0, z0 };
            case SOUTH -> new float[]{ x1, y0, z1,  x1, y1, z1,  x0, y1, z1,  x0, y0, z1 };
            case EAST ->  new float[]{ x1, y0, z0,  x1, y1, z0,  x1, y1, z1,  x1, y0, z1 };
            case WEST ->  new float[]{ x0, y0, z1,  x0, y1, z1,  x0, y1, z0,  x0, y0, z0 };
            default -> throw new IllegalArgumentException("Wrong direction for vertical quad: " + face);
        };
        putQuad(vertexConsumer, face, element, coords);
    }

    /**
     * Renders quad.
     * @param vertexConsumer vertex consumer
     * @param face           quad direction
     * @param element        LODs column element
     * @param coords         vertex coordinates
     */
    protected static void putQuad(VertexConsumer vertexConsumer, Direction face, LODsColumnElement element,
                                  float[] coords) {
        float[] normal = switch (face) {
            case UP -> NORMAL_UP;
            case DOWN -> NORMAL_DOWN;
            case NORTH -> NORMAL_NORTH;
            case SOUTH -> NORMAL_SOUTH;
            case EAST -> NORMAL_EAST;
            case WEST -> NORMAL_WEST;
        };

        for (int v = 0; v < 4; v++) {
            vertexConsumer.addVertex(coords[v * 3], coords[v * 3 + 1], coords[v * 3 + 2])
                    .setNormal(normal[0], normal[1], normal[2])
                    .setUv(DEFAULT_UV[v * 2], DEFAULT_UV[v * 2 + 1])
                    .setColor(element.color())
                    .setLight(element.blockLight() << 4);
        }
    }

    /**
     * Closes block color resolver.
     */
    @Override
    public void close() {
        this.colorResolver.close();
    }

    /**
     * Stores LODs column element info (portion of column with the same block state and biome).
     * @param bottomYBlockPos bottom Y block coordinate
     * @param topYBlockPos    top Y block coordinate
     * @param state           block state
     * @param color           RGB color
     * @param blockLight      block light level
     */
    public record LODsColumnElement(int bottomYBlockPos, int topYBlockPos, BlockState state,
                                    int color, int blockLight) {}

    /**
     * Contents of one cell in LODs column.
     * @param state      block state
     * @param biome      biome
     * @param blockLight block light level
     */
    protected record ColumnCell(@Nullable BlockState state, @Nullable Holder<Biome> biome, int blockLight) {
        /**
         * @return whether two cells have same contents.
         */
        boolean isSame(ColumnCell other) {
            return Objects.equals(state, other.state)
                    && Objects.equals(biome, other.biome)
                    && blockLight == other.blockLight;
        }
    }
}
