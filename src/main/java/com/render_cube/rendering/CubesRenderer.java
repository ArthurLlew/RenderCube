package com.render_cube.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.render_cube.vertex_consumers.BasicVertexConsumer;
import com.render_cube.vertex_consumers.CommonVertexConsumer;
import com.render_cube.vertex_consumers.FakeMultiBufferSource;
import com.render_cube.vertex_consumers.LiquidVertexConsumer;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Contains mod utils.
 **/
public class CubesRenderer {
    /**
     * Renders one cube.
     * @param level Minecraft level where procedure will run
     * @param fileWriters used to write captured data
     * @param levelPos block position in level
     * @param regionPos block position in region
     **/
    public static void renderCube(@NotNull Level level,
                                  @NotNull FileWriters fileWriters,
                                  @NotNull BlockPos levelPos,
                                  @NotNull BlockPos regionPos) {
        BlockState block = level.getBlockState(levelPos);

        // If block is not empty
        if (!block.isAir()) {
            CommonVertexConsumer commonVertexConsumer =
                    new CommonVertexConsumer(fileWriters.blockWriter, regionPos);

            // Block extra model data
            ModelData data = Minecraft.getInstance().getBlockRenderer().getBlockModel(block).getModelData(
                    level,
                    levelPos,
                    block,
                    ModelData.EMPTY);

            // Consume block vertices
            Minecraft.getInstance().getBlockRenderer().renderBatched(
                    block,
                    levelPos,
                    level,
                    new PoseStack(),
                    commonVertexConsumer,
                    true,
                    RandomSource.create(block.getSeed(levelPos)),
                    data,
                    null);

            // If there is fluid
            FluidState fluid = block.getFluidState();
            if (!fluid.isEmpty()){
                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer =
                        new LiquidVertexConsumer(fileWriters.liquidWriter, regionPos, levelPos);

                // Consume liquid vertices
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPos,
                        level,
                        liquidVertexConsumer,
                        block,
                        fluid);
            }

            // If there is a block-entity
            BlockEntity entity = level.getBlockEntity(levelPos);
            if(entity != null){
                FakeMultiBufferSource fakeMultiBufferSource =
                        new FakeMultiBufferSource(
                                new CommonVertexConsumer(fileWriters.blockEntityWriter, regionPos));

                // Render block-entity using dummy MultiBufferSource
                Minecraft.getInstance().getBlockEntityRenderDispatcher().render(
                        entity,
                        1.0F,
                        new PoseStack(),
                        fakeMultiBufferSource);
            }
        }
    }

    /**
     * Renders entities in region.
     * @param level Minecraft level where procedure will run
     * @param fileWriters used to write captured data
     * @param minPos min coordinate of the region to render
     * @param maxPos max coordinate of the region to render
     **/
    public static void renderRegionEntities(@NotNull Level level,
                                            @NotNull FileWriters fileWriters,
                                            @NotNull BlockPos minPos,
                                            @NotNull BlockPos maxPos){
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

            FakeMultiBufferSource fakeMultiBufferSource = new FakeMultiBufferSource(
                    new BasicVertexConsumer(fileWriters.entityWriter));

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
                    fakeMultiBufferSource,
                    entityRenderDispatcher.getPackedLightCoords(entity, minecraftConstant));
        }
    }

    /**
     * Renders world region.
     * @param level Minecraft level where procedure will run
     * @param fileWriters used to write captured data
     * @param minPos min coordinate of the region to render
     * @param maxPos max coordinate of the region to render
     **/
    public static void renderRegion(@NotNull Level level,
                                    @NotNull FileWriters fileWriters,
                                    @NotNull BlockPos minPos,
                                    @NotNull BlockPos maxPos){
        // Loop over coordinates inside the region
        for(int x = minPos.getX(); x <= maxPos.getX(); x++){
            for(int y = minPos.getY(); y <= maxPos.getY(); y++){
                for(int z = minPos.getZ(); z <= maxPos.getZ(); z++){
                    // Process cube
                    CubesRenderer.renderCube(level, fileWriters,
                            new BlockPos(x, y, z),
                            new BlockPos(x - minPos.getX(), y - minPos.getY(), z - minPos.getZ()));
                }
            }
        }

        // Process region entities
        CubesRenderer.renderRegionEntities(level, fileWriters, minPos, maxPos);
    }
}
