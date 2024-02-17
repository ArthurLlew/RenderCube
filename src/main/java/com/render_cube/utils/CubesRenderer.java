package com.render_cube.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.render_cube.vertex_consumers.BasicVertexConsumer;
import com.render_cube.vertex_consumers.CommonVertexConsumer;
import com.render_cube.vertex_consumers.FakeMultiBufferSource;
import com.render_cube.vertex_consumers.LiquidVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
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
     * @param fileWriters writers, used to write captured data
     * @param levelPosition block position in level
     * @param regionPosition block position in region
     **/
    public static void renderCube(@NotNull Level level,
                                  @NotNull FileWriters fileWriters,
                                  @NotNull BlockPos levelPosition,
                                  @NotNull BlockPos regionPosition) {
        // Get BlockState at position
        BlockState block = level.getBlockState(levelPosition);

        // We do not want to render air
        if (!block.isAir()) {
            // Init block consumer
            CommonVertexConsumer commonVertexConsumer =
                    new CommonVertexConsumer(fileWriters.blockWriter, regionPosition);

            // Block extra model data
            ModelData data = Minecraft.getInstance().getBlockRenderer().getBlockModel(block).getModelData(
                    level,
                    levelPosition,
                    block,
                    ModelData.EMPTY);

            // Consume block vertices
            Minecraft.getInstance().getBlockRenderer().renderBatched(
                    block,
                    levelPosition,
                    level,
                    new PoseStack(),
                    commonVertexConsumer,
                    true,
                    RandomSource.create(block.getSeed(levelPosition)),
                    data,
                    RenderType.solid());

            // If there is fluid
            FluidState fluid = block.getFluidState();
            if (!fluid.isEmpty()){
                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer =
                        new LiquidVertexConsumer(fileWriters.liquidWriter, regionPosition);

                // Consume liquid vertices
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPosition,
                        level,
                        liquidVertexConsumer,
                        block,
                        fluid);
            }

            // If there is a block-entity
            BlockEntity entity = level.getBlockEntity(levelPosition);
            if(entity != null){
                // Create dummy MultiBufferSource
                FakeMultiBufferSource fakeMultiBufferSource =
                        new FakeMultiBufferSource(
                                new CommonVertexConsumer(fileWriters.blockEntityWriter, regionPosition));

                // Render into dummy MultiBufferSource
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
     * @param fileWriters writers, used to write captured data
     * @param regionCoordinates array that holds region_min_x, region_min_y, region_min_z, region_max_x, region_max_y,
     *                          region_max_z positions of region in world
     **/
    public static void renderRegionEntities(@NotNull Level level,
                                            @NotNull FileWriters fileWriters,
                                            int[] regionCoordinates){
        // Get all entities in region (except player entity)
        List<Entity> entities = level.getEntities(
                (Entity)null, new AABB(
                        regionCoordinates[0],
                        regionCoordinates[1],
                        regionCoordinates[2],
                        regionCoordinates[3],
                        regionCoordinates[4],
                        regionCoordinates[5]),
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

            // Create dummy MultiBufferSource
            FakeMultiBufferSource fakeMultiBufferSource = new FakeMultiBufferSource(
                    new BasicVertexConsumer(fileWriters.entityWriter));

            // Render entity into dummy MultiBufferSource
            entityRenderDispatcher.render(
                    entity,
                    entityX - regionCoordinates[0],
                    entityY - regionCoordinates[1],
                    entityZ - regionCoordinates[2],
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
     * @param fileWriters writers, used to write captured data
     * @param regionCoordinates coordinates of region to render
     * @see CubesRenderer#renderRegionEntities(Level, FileWriters, int[])
     **/
    public static void renderRegion(@NotNull Level level,
                                    @NotNull FileWriters fileWriters,
                                    int[] regionCoordinates){
        // Loop over coordinates included in region
        for(int x = regionCoordinates[0]; x <= regionCoordinates[3]; x++){
            for(int y = regionCoordinates[1]; y <= regionCoordinates[4]; y++){
                for(int z = regionCoordinates[2]; z <= regionCoordinates[5]; z++){
                    // Process cube
                    CubesRenderer.renderCube(
                            level,
                            fileWriters,
                            new BlockPos(x, y, z),
                            new BlockPos(
                                    x - regionCoordinates[0],
                                    y - regionCoordinates[1],
                                    z - regionCoordinates[2])
                    );
                }
            }
        }

        // Process region entities
        CubesRenderer.renderRegionEntities(level, fileWriters, regionCoordinates);
    }
}
