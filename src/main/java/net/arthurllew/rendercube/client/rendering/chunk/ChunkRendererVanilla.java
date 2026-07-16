package net.arthurllew.rendercube.client.rendering.chunk;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arthurllew.rendercube.client.io.DataWriters;
import net.arthurllew.rendercube.client.rendering.vertex.CommonVertexConsumer;
import net.arthurllew.rendercube.client.rendering.vertex.LiquidVertexConsumer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
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

import java.io.IOException;
import java.util.List;

public class ChunkRendererVanilla extends ChunkRenderer {
    /**
     * Level accessor.
     */
    protected final Level level;

    /**
     * Constructor.
     * @param level level accessor
     */
    public ChunkRendererVanilla(Level level) {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     **/
    @Override
    public void captureChunk(@NotNull DataWriters dataWriters,
                             @NotNull BlockPos regionMin, @NotNull BlockPos regionMax,
                             @NotNull ChunkPos chunkPos) throws IOException {
        // Determine rendering bounds inside chunk
        int minX = Math.max(chunkPos.getMinBlockX(), regionMin.getX());
        int maxX = Math.min(chunkPos.getMaxBlockX(), regionMax.getX());
        int minZ = Math.max(chunkPos.getMinBlockZ(), regionMin.getZ());
        int maxZ = Math.min(chunkPos.getMaxBlockZ(), regionMax.getZ());

        // Loop over coordinates inside chunk
        for(int x = minX; x <= maxX; x++){
            for(int y = regionMin.getY(); y <= regionMax.getY(); y++){
                for(int z = minZ; z <= maxZ; z++){
                    // Capture block
                    captureBlock(dataWriters, this.level,
                            new BlockPos(x, y, z),
                            new BlockPos(x - regionMin.getX(), y - regionMin.getY(), z - regionMin.getZ()));
                }
            }
        }

        // Capture chunk entities
        captureEntities(dataWriters, this.level,
                new BlockPos(chunkPos.getMinBlockX(), regionMin.getY(), chunkPos.getMinBlockZ()),
                new BlockPos(chunkPos.getMaxBlockX(), regionMax.getY(), chunkPos.getMaxBlockZ()),
                new BlockPos(
                        chunkPos.getMinBlockX() - regionMin.getX(),
                        0,
                        chunkPos.getMinBlockZ() - regionMin.getZ()));
    }

    /**
     * Captures geometry of one block.
     * @param dataWriters used to write captured data
     * @param level       level accessor
     * @param levelPos    level relative block position
     * @param regionPos   region relative block position
     **/
    public static void captureBlock(@NotNull DataWriters dataWriters,
                                    @NotNull Level level,
                                    @NotNull BlockPos levelPos,
                                    @NotNull BlockPos regionPos) throws IOException {
        // Get block at current position
        BlockState blockState = level.getBlockState(levelPos);

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
            RandomSource randomSource = RandomSource.create(blockState.getSeed(levelPos));
            blockRenderDispatcher.renderBatched(
                    blockState,
                    levelPos,
                    level,
                    new PoseStack(),
                    blockVertexConsumer,
                    blockConsumerSettings == null || blockConsumerSettings.cullSides(),
                    randomSource);

            // If there is a fluid
            FluidState fluid = blockState.getFluidState();
            consumerName = blockConsumerSettings != null ? blockConsumerSettings.filename() : "renderedLiquids";
            if (!fluid.isEmpty()){
                // Render liquid
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPos,
                        level,
                        new LiquidVertexConsumer(dataWriters.get(consumerName), regionPos, levelPos),
                        blockState,
                        fluid);
            }

            // If there is a block-entity
            BlockEntity blockEntity = level.getBlockEntity(levelPos);
            if(blockEntity != null){
                // Render block-entity using wrapped vertex consumer
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
     * Captures geometry of entities inside a chunk.
     * @param dataWriters used to write captured data
     * @param level       level accessor
     * @param chunkMin    min block position of the chunk to capture
     * @param chunkMax    max block position of the chunk to capture
     * @param regionPos   region relative block position
     **/
    public static void captureEntities(@NotNull DataWriters dataWriters, @NotNull Level level,
                                       @NotNull BlockPos chunkMin, @NotNull BlockPos chunkMax,
                                       @NotNull BlockPos regionPos) throws IOException {
        // Get all entities in region (except player entity)
        List<Entity> entities = level.getEntities(
                (Entity)null, new AABB(
                        chunkMin.getX(),
                        chunkMin.getY(),
                        chunkMin.getZ(),
                        chunkMax.getX(),
                        chunkMax.getY(),
                        chunkMax.getZ()),
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
                    entityX - chunkMin.getX(),
                    entityY - chunkMin.getY(),
                    entityZ - chunkMin.getZ(),
                    // This float describes entity rotation
                    Mth.lerp(minecraftConstant, entity.yRotO, entity.getYRot()),
                    minecraftConstant,
                    new PoseStack(),
                    new CommonVertexConsumer(dataWriters.get("renderedEntities"), regionPos).wrap(),
                    entityRenderDispatcher.getPackedLightCoords(entity, minecraftConstant));
        }
    }
}
