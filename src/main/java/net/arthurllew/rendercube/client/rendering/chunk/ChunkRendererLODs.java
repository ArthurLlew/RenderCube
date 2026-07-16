package net.arthurllew.rendercube.client.rendering.chunk;

import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.arthurllew.rendercube.client.io.DataWriters;
import net.arthurllew.rendercube.client.rendering.RegionRenderer;
import net.arthurllew.rendercube.client.rendering.vertex.CommonVertexConsumer;
import net.arthurllew.rendercube.client.texture.LODsColorResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
    Long2ObjectLinkedOpenHashMap<LODsColumnElement[]> columnsCache = new Long2ObjectLinkedOpenHashMap<>(COLUMN_CACHE_CAPACITY);

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

        // Existing column
        if ((column = this.columnsCache.get(key)) != null) {
            return column;
        }
        // Generate a new one
        else {
            // Cache size must remain below capacity
            if (this.columnsCache.size() >= COLUMN_CACHE_CAPACITY) {
                this.columnsCache.removeFirst();
            }

            column = this.buildLODsColumn(x, z, minY, maxY, this.columnWidth);
            this.columnsCache.put(key, column);
            return column;
        }
    }

    /**
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

        // All block states and biomes in one column cell
        Map<BlockState, Integer> columnCellBlockStates = new HashMap<>();
        Map<Holder<Biome>, Integer> columnCellBiomes = new HashMap<>();

        // Block states and biomes in each column cell (chosen from previous maps)
        BlockState[] columnBlockStates = new BlockState[height];
        @SuppressWarnings("unchecked") // should not break stuff in theory
        Holder<Biome>[] columnBiomes = (Holder<Biome>[])new Holder[height];

        // Prepare mutable block position
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // Iterate over Y inside column
        for (int y = minY; y < maxY; y++) {
            // New cell voting
            columnCellBlockStates.clear();
            columnCellBiomes.clear();

            // Iterate over XZ inside column
            for (int iX = 0; iX < columnWidth; iX++) {
                for (int iZ = 0; iZ < columnWidth; iZ++) {
                    // Get block state and biome at current position
                    pos.set(x + iX, y, z + iZ);
                    BlockState block = this.level.getBlockState(pos);
                    Holder<Biome> biome = this.level.getNoiseBiome(pos.getX() >> 2, pos.getY() >> 2, pos.getZ() >> 2);

                    // If block is not empty
                    if (!block.isAir()) {
                        // Add to vote
                        columnCellBlockStates.merge(block, 1, Integer::sum);
                        columnCellBiomes.merge(biome, 1, Integer::sum);
                    }
                }
            }

            // Choose single block state and biome as key with max value
            columnBlockStates[y - minY] = columnCellBlockStates.isEmpty() ?
                    null : Collections.max(columnCellBlockStates.entrySet(), Map.Entry.comparingByValue()).getKey();
            columnBiomes[y - minY] = columnCellBiomes.isEmpty() ?
                    null : Collections.max(columnCellBiomes.entrySet(), Map.Entry.comparingByValue()).getKey();
        }

        // Init LODs column
        List<LODsColumnElement> column = new ArrayList<>();

        // Bottom element (used for culling)
        int prevY = minY;
        BlockState prevState = columnBlockStates[0];
        Holder<Biome> prevBiome = columnBiomes[0];
        // Put it as is
        addColumnElement(column, x, z, prevY, prevY+1, prevState);

        // Iterate column
        for (int i = 1; i < height - 1; i++) {
            // Current cell block state and biome
            BlockState state = columnBlockStates[i];
            Holder<Biome> biome = columnBiomes[i];

            // If current cell differs from previous or serves as offset
            if ((state != null && !state.equals(prevState))
                    || (biome != null && !biome.equals(prevBiome))
                    || (prevState != null && !prevState.equals(state))
                    || (prevBiome != null && !prevBiome.equals(biome))) {
                // Y block coordinate
                int y = minY + i;

                // Add previous column data point
                addColumnElement(column, x, z, prevY, y, prevState);
                // Remember current column data point state
                prevY = y;
                prevState = state;
                prevBiome = biome;
            }
        }
        // Add the last column data point
        addColumnElement(column, x, z, prevY, maxY-1, prevState);

        // Top element (used for culling)
        addColumnElement(column, x, z, maxY-1, maxY, columnBlockStates[height-1]);

        // Convert to array and return
        return column.toArray(new LODsColumnElement[0]);
    }

    /**
     * Adds LODs column element (portion of column with the same block state and biome).
     * @param column          chunk column
     * @param x               X block coordinate
     * @param z               Z block coordinate
     * @param bottomYBlockPos column element bottom Y block coordinate
     * @param topYBlockPos    column element top Y block coordinate
     * @param state           cell block
     */
    protected void addColumnElement(@NotNull List<LODsColumnElement> column,
                                    int x, int z, int bottomYBlockPos, int topYBlockPos,
                                    @Nullable BlockState state) {
        // Skip empty cell
        if (state == null || topYBlockPos <= bottomYBlockPos) return;

        // Position in the middle of column element
        BlockPos middlePos = new BlockPos(
                x + this.columnWidth/2,
                bottomYBlockPos + (topYBlockPos - bottomYBlockPos) / 2,
                z + this.columnWidth/2);

        // Add column element
        column.add(new LODsColumnElement(bottomYBlockPos, topYBlockPos, state,
                this.colorResolver.getBlockColor(this.level, state, middlePos)));
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

            // XZ being on render region boarder flags
            boolean isOnRegionBoarderX = x0 == 0 || x1 == regionSizeX;
            boolean isOnRegionBoarderZ = z0 == 0 || z1 == regionSizeZ;

            // If bottom is not blocked or face is on region boarder and boarder culling is off
            LODsColumnElement below = (i > 0) ? column[i - 1] : null;
            if (below == null || below.topYBlockPos() < element.bottomYBlockPos()
                    || below.state().is(Blocks.WATER)
                    || (RegionRenderer.STATE.noRenderRegionBoarderFaceCulling() && y0 == regionMin.getY())) {
                // Render DOWN face
                putHorizontalQuad(vertexConsumer, Direction.DOWN, element, x0, z0, x1, z1, y0);
            }

            // Render SIDE faces
            putVerticalSide(vertexConsumer, Direction.NORTH, element,
                    getNeighboringColumn(minX, minZ - this.columnWidth, regionMin, regionMax, isOnRegionBoarderZ),
                    x0, z0, x1, z1, y0, y1);
            putVerticalSide(vertexConsumer, Direction.WEST, element,
                    getNeighboringColumn(minX - this.columnWidth, minZ, regionMin, regionMax, isOnRegionBoarderX),
                    x0, z0, x1, z1, y0, y1);
            putVerticalSide(vertexConsumer, Direction.SOUTH, element,
                    getNeighboringColumn(minX, minZ + this.columnWidth, regionMin, regionMax, isOnRegionBoarderZ),
                    x0, z0, x1, z1, y0, y1);
            putVerticalSide(vertexConsumer, Direction.EAST, element,
                    getNeighboringColumn(minX + this.columnWidth, minZ, regionMin, regionMax, isOnRegionBoarderX),
                    x0, z0, x1, z1, y0, y1);

            // If top is not blocked or face is on region boarder and boarder culling is off
            LODsColumnElement above = (i < column.length - 1) ? column[i + 1] : null;
            if (above == null || above.bottomYBlockPos() > element.topYBlockPos()
                    || above.state().is(Blocks.WATER)
                    || (RegionRenderer.STATE.noRenderRegionBoarderFaceCulling() && y1 == regionMax.getY())) {
                // Render UP face
                putHorizontalQuad(vertexConsumer, Direction.UP, element, x0, z0, x1, z1, y1);
            }
        }
    }

    /**
     * @param x                 LODs column X world position
     * @param z                 LODs column Z world position
     * @param regionMin   min block position of the region to capture
     * @param regionMax   max block position of the region to capture
     * @param isOnRegionBoarder whether the side is on render region boarder
     * @return neighboring LODs column
     */
    protected LODsColumnElement[] getNeighboringColumn(int x, int z,
                                                       @NotNull BlockPos regionMin, @NotNull BlockPos regionMax,
                                                       boolean isOnRegionBoarder) {
        // Return neighbor if face is either not on render region boarder or boarder culling is on
        return (RegionRenderer.STATE.noRenderRegionBoarderFaceCulling() && isOnRegionBoarder) ?
                null : this.getLODsColumn(x, z, regionMin.getY() - 1, regionMax.getY() + 1);
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
        // Exposed areas of column
        List<float[]> exposedAreas = new ArrayList<>();
        // initialized as entire column
        exposedAreas.add(new float[]{y0, y1});

        // If column has neighbor in required direction
        if (neighboringColumn != null) {
            // Iterate neighboring column and update exposed areas
            for (LODsColumnElement neighboringElement : neighboringColumn) {
                // If neighboring column element is not empty and is of same type as current element (block/water)
                if (neighboringElement != null
                        && (neighboringElement.state().is(Blocks.WATER) == element.state().is(Blocks.WATER))) {
                    updateExposedAreas(exposedAreas,
                            neighboringElement.bottomYBlockPos(), neighboringElement.topYBlockPos());
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
        // Init resu;t
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
                    .setColor(element.color())
                    .setUv(DEFAULT_UV[v * 2], DEFAULT_UV[v * 2 + 1])
                    .setNormal(normal[0], normal[1], normal[2]);
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
     * @param state           block
     * @param color           RGB color
     */
    public record LODsColumnElement(int bottomYBlockPos, int topYBlockPos, BlockState state, int color) {}
}
