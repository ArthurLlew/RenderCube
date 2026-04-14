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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class CubesRenderer {
    /**
     * Renders one cube.
     * @param level Minecraft level where procedure will run
     * @param dataWriters used to write captured data
     * @param levelPos block position in level
     * @param regionPos block position in region
     **/
    public static void renderCube(@NotNull Level level,
                                  @NotNull DataWriters dataWriters,
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
     * Renders entities in region.
     * @param level Minecraft level where procedure will run
     * @param dataWriters used to write captured data
     * @param minPos min coordinate of the region to render
     * @param maxPos max coordinate of the region to render
     **/
    public static void renderRegionEntities(@NotNull Level level,
                                            @NotNull DataWriters dataWriters,
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

            // Render entity using dummy MultiBufferSource
            entityRenderDispatcher.render(
                    entity,
                    entityX - minPos.getX(),
                    entityY - minPos.getY(),
                    entityZ - minPos.getZ(),
                    // This float stands for entity rotation
                    Mth.lerp(minecraftConstant, entity.yRotO, entity.getYRot()),
                    minecraftConstant,
                    new PoseStack(),
                    new BasicVertexConsumer(dataWriters.get("renderedEntities")).wrap(),
                    entityRenderDispatcher.getPackedLightCoords(entity, minecraftConstant));
        }
    }

    /**
     * Renders world region.
     * @param level Minecraft level where procedure will run
     * @param dataWriters used to write captured data
     * @param minPos min coordinate of the region to render
     * @param maxPos max coordinate of the region to render
     **/
    public static void renderRegion(@NotNull Level level,
                                    @NotNull DataWriters dataWriters,
                                    @NotNull BlockPos minPos,
                                    @NotNull BlockPos maxPos) throws IOException {
        // Loop over coordinates inside the region
        for(int x = minPos.getX(); x <= maxPos.getX(); x++){
            for(int y = minPos.getY(); y <= maxPos.getY(); y++){
                for(int z = minPos.getZ(); z <= maxPos.getZ(); z++){
                    // Process cube
                    CubesRenderer.renderCube(level, dataWriters,
                            new BlockPos(x, y, z),
                            new BlockPos(x - minPos.getX(), y - minPos.getY(), z - minPos.getZ()));
                }
            }
        }

        // Process region entities
        CubesRenderer.renderRegionEntities(level, dataWriters, minPos, maxPos);
    }
}
