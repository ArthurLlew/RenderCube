package com.rendercube.rendering;

import com.rendercube.vertex_consumers.BasicVertexConsumer;
import com.rendercube.vertex_consumers.CommonVertexConsumer;
import com.rendercube.vertex_consumers.FakeMultiBufferSource;
import com.rendercube.vertex_consumers.LiquidVertexConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Contains mod utils.
 **/
public class CubesRenderer {
    /**
     * Renders one cube.
     * @param world Minecraft world where procedure will run
     * @param fileWriters used to write captured data
     * @param worldPos block position in world
     * @param regionPos block position in region
     **/
    public static void renderCube(@NotNull World world,
                                  @NotNull FileWriters fileWriters,
                                  @NotNull BlockPos worldPos,
                                  @NotNull BlockPos regionPos) {
        BlockState block = world.getBlockState(worldPos);

        // If block is not empty
        if (!block.isAir()) {
            CommonVertexConsumer commonVertexConsumer =
                    new CommonVertexConsumer(fileWriters.blockWriter, regionPos);

            // Consume block vertices
            Random random = Random.create(block.getRenderingSeed(worldPos));
            MinecraftClient.getInstance().getBlockRenderManager().renderBlock(
                    block,
                    worldPos,
                    world,
                    new MatrixStack(),
                    commonVertexConsumer,
                    true,
                    random);

            // If there is a fluid
            FluidState fluid = block.getFluidState();
            if (!fluid.isEmpty()){
                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer =
                        new LiquidVertexConsumer(fileWriters.liquidWriter, regionPos, worldPos);

                // Consume liquid vertices
                MinecraftClient.getInstance().getBlockRenderManager().renderFluid(
                        worldPos,
                        world,
                        liquidVertexConsumer,
                        block,
                        fluid);
            }

            // If there is a block-entity
            BlockEntity entity = world.getBlockEntity(worldPos);
            if(entity != null){
                FakeMultiBufferSource fakeMultiBufferSource =
                        new FakeMultiBufferSource(
                                new CommonVertexConsumer(fileWriters.blockEntityWriter, regionPos));

                // Render block-entity using dummy MultiBufferSource
                MinecraftClient.getInstance().getBlockEntityRenderDispatcher().render(
                        entity,
                        1.0F,
                        new MatrixStack(),
                        fakeMultiBufferSource);
            }
        }
    }

    /**
     * Renders entities in region.
     * @param world Minecraft world where procedure will run
     * @param fileWriters used to write captured data
     * @param minPos min coordinate of the region to render
     * @param maxPos max coordinate of the region to render
     **/
    public static void renderRegionEntities(@NotNull World world,
                                            @NotNull FileWriters fileWriters,
                                            @NotNull BlockPos minPos,
                                            @NotNull BlockPos maxPos){
        // Get all entities in region (except player entity)
        List<Entity> entities = world.getEntitiesByClass(
                Entity.class, new Box(
                        minPos.getX(),
                        minPos.getY(),
                        minPos.getZ(),
                        maxPos.getX(),
                        maxPos.getY(),
                        maxPos.getZ()),
                (entity) -> !(entity instanceof PlayerEntity));

        // Saves instance of minecraft entity render dispatcher for multiple use in loop
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        // 1.0F is a constant, that is parsed to such method by game to world renderer
        float minecraftConstant = 1.0F;

        // Process entities
        for (Entity entity: entities) {
            // Got here from game code of world renderer
            double entityX = MathHelper.lerp(minecraftConstant, entity.prevX, entity.getX());
            double entityY = MathHelper.lerp(minecraftConstant, entity.prevY, entity.getY());
            double entityZ = MathHelper.lerp(minecraftConstant, entity.prevZ, entity.getZ());

            FakeMultiBufferSource fakeMultiBufferSource = new FakeMultiBufferSource(
                    new BasicVertexConsumer(fileWriters.entityWriter));

            // Render entity using dummy MultiBufferSource
            entityRenderDispatcher.render(
                    entity,
                    entityX - minPos.getX(),
                    entityY - minPos.getY(),
                    entityZ - minPos.getZ(),
                    // This float stands for entity rotation
                    MathHelper.lerp(minecraftConstant, entity.getPitch(), entity.getYaw()),
                    minecraftConstant,
                    new MatrixStack(),
                    fakeMultiBufferSource,
                    entityRenderDispatcher.getLight(entity, minecraftConstant));
        }
    }

    /**
     * Renders world region.
     * @param world Minecraft world where procedure will run
     * @param fileWriters used to write captured data
     * @param minPos min coordinate of the region to render
     * @param maxPos max coordinate of the region to render
     **/
    public static void renderRegion(@NotNull World world,
                                    @NotNull FileWriters fileWriters,
                                    @NotNull BlockPos minPos,
                                    @NotNull BlockPos maxPos){
        // Loop over coordinates inside the region
        for(int x = minPos.getX(); x <= maxPos.getX(); x++){
            for(int y = minPos.getY(); y <= maxPos.getY(); y++){
                for(int z = minPos.getZ(); z <= maxPos.getZ(); z++){
                    // Process cube
                    CubesRenderer.renderCube(world, fileWriters,
                            new BlockPos(x, y, z),
                            new BlockPos(x - minPos.getX(), y - minPos.getY(), z - minPos.getZ()));
                }
            }
        }

        // Process region entities
        CubesRenderer.renderRegionEntities(world, fileWriters, minPos, maxPos);
    }
}
