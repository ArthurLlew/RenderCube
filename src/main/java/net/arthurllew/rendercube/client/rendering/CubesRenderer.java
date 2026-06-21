package net.arthurllew.rendercube.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arthurllew.rendercube.client.io.DataWriters;
import net.arthurllew.rendercube.client.rendering.vertex.BasicVertexConsumer;
import net.arthurllew.rendercube.client.rendering.vertex.CommonVertexConsumer;
import net.arthurllew.rendercube.client.rendering.vertex.LiquidVertexConsumer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class CubesRenderer {
    /**
     * Renderer state.
     */
    public static final State STATE = new State();

    /**
     * Captures geometry of one block.
     * @param dataWriters used to write captured data
     * @param level Minecraft
     * @param pos block position in level
     * @param regionPos block position in region
     **/
    public static void captureBlock(@NotNull DataWriters dataWriters,
                                    @NotNull Level level,
                                    @NotNull BlockPos pos,
                                    @NotNull BlockPos regionPos) throws IOException {
        // Get block at current position
        BlockState blockState = level.getBlockState(pos);

        // If block is not empty
        if (!blockState.isAir()) {
            // Check if this block has custom settings
            Config.Data.BlockConsumerConfig.BlockConsumerSettings blockConsumerSettings = null;
            for (Config.Data.BlockConsumerConfig blockConsumerConfig : Config.DATA.blockConsumerConfigs) {
                // Try to get custom settings
                blockConsumerSettings = blockConsumerConfig.getSettings(blockState);
                // Stop loop on find
                if (blockConsumerSettings != null) {
                    break;
                }
            }

            // Init block vertex consumer
            String consumerName = blockConsumerSettings != null ? blockConsumerSettings.filename() : "renderedBlocks";
            CommonVertexConsumer blockVertexConsumer =
                    new CommonVertexConsumer(dataWriters.get(consumerName), regionPos);

            // Consume block vertices for every render type available
            BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            RandomSource randomSource = RandomSource.create(blockState.getSeed(pos));
            blockRenderDispatcher.renderBatched(
                    blockState,
                    pos,
                    level,
                    new PoseStack(),
                    blockVertexConsumer,
                    blockConsumerSettings == null || blockConsumerSettings.cullSides(),
                    randomSource);

            // If there is a fluid
            FluidState fluid = blockState.getFluidState();
            if (!fluid.isEmpty()){
                // Render liquid
                consumerName = blockConsumerSettings != null ? blockConsumerSettings.filename() : "renderedLiquids";
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        pos,
                        level,
                        new LiquidVertexConsumer(dataWriters.get(consumerName), regionPos, pos),
                        blockState,
                        fluid);
            }

            // If there is a block-entity
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity != null){
                // Render block-entity using dummy MultiBufferSource
                consumerName = blockConsumerSettings != null ? blockConsumerSettings.filename() : "renderedBlockEntities";
                Minecraft.getInstance().getBlockEntityRenderDispatcher().render(
                        blockEntity,
                        1.0F,
                        new PoseStack(),
                        new CommonVertexConsumer(dataWriters.get(consumerName), regionPos).wrap());
            }
        }
    }

    /**
     * Captures geometry of entities in region.
     * @param dataWriters used to write captured data
     * @param level Minecraft level
     * @param minPos min block position of the region to capture
     * @param maxPos max block position of the region to capture
     **/
    public static void captureEntities(@NotNull DataWriters dataWriters,
                                       @NotNull Level level,
                                       @NotNull BlockPos minPos,
                                       @NotNull BlockPos maxPos) throws IOException {
        // Get all entities in region (except player entity)
        List<Entity> entities = level.getEntities(
                (Entity)null, new AABB(
                        minPos.getX(),
                        minPos.getY(),
                        minPos.getZ(),
                        maxPos.getX(),
                        maxPos.getY(),
                        maxPos.getZ()),
                (entity) -> !(entity instanceof Player));

        // Saves instance of minecraft entity render dispatcher for multiple use in loop
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        // 1.0F is a constant, that is parsed to such method by game to level renderer
        float minecraftConstant = 1.0F;

        // Process entities
        for (Entity entity: entities) {
            // Got here from game code of level renderer
            double entityX = Mth.lerp(minecraftConstant, entity.xOld, entity.getX());
            double entityY = Mth.lerp(minecraftConstant, entity.yOld, entity.getY());
            double entityZ = Mth.lerp(minecraftConstant, entity.zOld, entity.getZ());

            // Render entity using wrapped vertex consumer
            entityRenderDispatcher.render(
                    entity,
                    entityX - minPos.getX(),
                    entityY - minPos.getY(),
                    entityZ - minPos.getZ(),
                    // This float describes entity rotation
                    Mth.lerp(minecraftConstant, entity.yRotO, entity.getYRot()),
                    minecraftConstant,
                    new PoseStack(),
                    new BasicVertexConsumer(dataWriters.get("renderedEntities")).wrap(),
                    entityRenderDispatcher.getPackedLightCoords(entity, minecraftConstant));
        }
    }

    /**
     * Captures geometry of one chunk.
     * @param dataWriters used to write captured data
     * @param level Minecraft level
     * @param minPos min block position of the region to capture
     * @param maxPos max block position of the region to capture
     * @param chunkPos position of chunk being rendered
     **/
    public static void captureChunk(@NotNull DataWriters dataWriters,
                                    @NotNull Level level,
                                    @NotNull BlockPos minPos,
                                    @NotNull BlockPos maxPos,
                                    @NotNull ChunkPos chunkPos) throws IOException {
        // Determine rendering bounds inside chunk
        int minX = Math.max(chunkPos.getMinBlockX(), minPos.getX());
        int maxX = Math.min(chunkPos.getMaxBlockX(), maxPos.getX());
        int minZ = Math.max(chunkPos.getMinBlockZ(), minPos.getZ());
        int maxZ = Math.min(chunkPos.getMaxBlockZ(), maxPos.getZ());

        // Loop over coordinates inside chunk
        for(int x = minX; x <= maxX; x++){
            for(int y = minPos.getY(); y <= maxPos.getY(); y++){
                for(int z = minZ; z <= maxZ; z++){
                    // Capture block
                    CubesRenderer.captureBlock(dataWriters, level,
                            new BlockPos(x, y, z),
                            new BlockPos(x - minPos.getX(), y - minPos.getY(), z - minPos.getZ()));
                }
            }
        }

        // Capture chunk entities
        CubesRenderer.captureEntities(dataWriters, level,
                new BlockPos(chunkPos.getMinBlockX(), minPos.getY(), chunkPos.getMinBlockZ()),
                new BlockPos(chunkPos.getMaxBlockX(), maxPos.getY(), chunkPos.getMaxBlockZ()));
    }

    /**
     * Captures geometry of world region.
     * @param dataWriters used to write captured data
     * @param level Minecraft level
     * @param minPos min block position of the region to capture
     * @param maxPos max block position of the region to capture
     **/
    public static void captureRegion(@Nullable DataWriters dataWriters,
                                     @NotNull Level level,
                                     @NotNull BlockPos minPos,
                                     @NotNull BlockPos maxPos) throws IOException {
        // Only if rendering
        if (STATE.isRendering) {
            // Loop over chunks in region
            for(int chunkX = SectionPos.blockToSectionCoord(minPos.getX());
                chunkX <= SectionPos.blockToSectionCoord(maxPos.getX());
                chunkX++){
                for(int chunkZ = SectionPos.blockToSectionCoord(minPos.getZ());
                    chunkZ <= SectionPos.blockToSectionCoord(maxPos.getZ());
                    chunkZ++){
                    // Capture chunk with unified data writers
                    if (dataWriters != null) {
                        CubesRenderer.captureChunk(dataWriters, level, minPos, maxPos,
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
                            CubesRenderer.captureChunk(chunkDataWriters, level, minPos, maxPos,
                                    new ChunkPos(chunkX, chunkZ));
                        }
                    }
                }
            }
        }
    }

    /**
     * Captures geometry of world region.
     * @param usePerChunkRendering whether to write data in unified files or split data files per chunk
     * @param level Minecraft level
     * @param minPos min block position of the region to capture
     * @param maxPos max block position of the region to capture
     * @param noRenderRegionBoarderFaceCulling whether to render faces on render region boarder
     **/
    public static void captureRegion(boolean usePerChunkRendering,
                                     @NotNull Level level,
                                     @NotNull BlockPos minPos,
                                     @NotNull BlockPos maxPos,
                                     boolean noRenderRegionBoarderFaceCulling) throws IOException {
        // Only render if not rendering
        if (!STATE.isRendering) {
            // Activate renderer
            STATE.isRendering = true;
            STATE.noRenderRegionBoarderFaceCulling = noRenderRegionBoarderFaceCulling;
            STATE.setRegionPositions(minPos, maxPos);
            // Date time string
            String dateTimeStr = LocalDateTime.now().toString()
                    .replace("T", "_").replace(":", "-");
            STATE.dateTime = dateTimeStr.substring(0, dateTimeStr.lastIndexOf("."));

            // Capture region with per-chunk data writers
            if (usePerChunkRendering) {
                captureRegion(null, level, minPos, maxPos);
            }
            // Capture region with unified data writers
            else {
                try (DataWriters dataWriters = new DataWriters(STATE.dateTime, (name) -> name)) {
                    captureRegion(dataWriters, level, minPos, maxPos);
                }
            }

            // Stop renderer
            STATE.isRendering = false;
        }
    }

    /**
     * Renderer state.
     */
    public static class State {
        protected State(){}

        /**
         * Whether any rendering is in process.
         */
        protected boolean isRendering = false;
        /**
         * Region positions.
         */
        protected BlockPos posMin, posMax = BlockPos.ZERO;
        /**
         * Whether to not cull quads facing out of render region and located on its boarder.
         */
        protected boolean noRenderRegionBoarderFaceCulling = false;
        /**
         * Datetime of rendering.
         */
        protected String dateTime = null;

        /**
         * @return whether any rendering is in process.
         */
        public boolean isRendering() {
            return isRendering;
        }

        /**
         * Sets region positions.
         */
        protected void setRegionPositions(BlockPos posMin, BlockPos posMax) {
            this.posMin = posMin;
            this.posMax = posMax;
        }

        public boolean noRenderRegionBoarderFaceCulling() {
            return noRenderRegionBoarderFaceCulling;
        }

        /**
         * @return whether provided position is outside render region
         */
        public boolean isOutsideRenderRegion(BlockPos pos) {
            return pos.getX() < this.posMin.getX()
                    || pos.getY() < this.posMin.getY()
                    || pos.getZ() < this.posMin.getZ()
                    || pos.getX() > this.posMax.getX()
                    || pos.getY() > this.posMax.getY()
                    || pos.getZ() > this.posMax.getZ();
        }
    }
}