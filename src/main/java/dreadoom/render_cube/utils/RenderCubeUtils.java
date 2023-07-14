package dreadoom.render_cube.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.vertex_consumers.BasicVertexConsumer;
import dreadoom.render_cube.vertex_consumers.CommonVertexConsumer;
import dreadoom.render_cube.vertex_consumers.DummyMultiBufferSource;
import dreadoom.render_cube.vertex_consumers.LiquidVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

/**
 * Contains mod utils.
 **/
public class RenderCubeUtils{
    /**
     * Checks if this mod special directory exists, and, if not, creates it.
     **/
    public static void checkAndCreateModDir() throws IOException {
        // Check if this mod special directory exists
        Path path = Paths.get(System.getProperty("user.dir") + "\\" + RenderCube.MODID);

        // If not
        if (!Files.exists(path)) {
            // create it
            Files.createDirectories(path);
        }
    }

    /**
     * Renders one cube.
     * @param source command executioner
     * @param fileWriters writers, used to write captured data
     * @param levelPosition block position in level
     * @param regionPosition block position in region
     **/
    public static void renderCube(@NotNull CommandSourceStack source,
                                  @NotNull FileWriters fileWriters,
                                  @NotNull BlockPos levelPosition,
                                  @NotNull BlockPos regionPosition) {
        // Get level, where command is executed
        ServerLevel level = source.getLevel();

        // Get BlockState at position
        BlockState block = level.getBlockState(levelPosition);

        // We do not want to render air
        if (!block.isAir()) {
            // Init block consumer
            CommonVertexConsumer commonVertexConsumer =
                    new CommonVertexConsumer(fileWriters.blockWriter, regionPosition);

            // Init random with block seed at this position
            Random random = new Random(block.getSeed(levelPosition));

            // Block extra model data
            IModelData data = Minecraft.getInstance().getBlockRenderer().getBlockModel(block).getModelData(
                    level,
                    levelPosition,
                    block,
                    EmptyModelData.INSTANCE);

            // Consume block vertices
            Minecraft.getInstance().getBlockRenderer().renderBatched(
                    block,
                    levelPosition,
                    level,
                    new PoseStack(),
                    commonVertexConsumer,
                    true,
                    random,
                    data);

            // If this block contains fluid
            if (!block.getFluidState().isEmpty()){
                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer =
                        new LiquidVertexConsumer(fileWriters.liquidWriter, regionPosition, levelPosition);

                // Consume liquid vertices
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPosition,
                        level,
                        liquidVertexConsumer,
                        block,
                        block.getFluidState());
            }

            // Get entity at our position
            BlockEntity entity = level.getBlockEntity(levelPosition);
            // If it is not null
            if(entity != null){
                // Create dummy MultiBufferSource
                DummyMultiBufferSource dummyMultiBufferSource =
                        new DummyMultiBufferSource(
                                new CommonVertexConsumer(fileWriters.blockEntityWriter, regionPosition));

                // Render into dummy MultiBufferSource
                Minecraft.getInstance().getBlockEntityRenderDispatcher().render(
                        entity,
                        1.0F,
                        new PoseStack(),
                        dummyMultiBufferSource);
            }
        }
    }

    /**
     * Renders all entities in region.
     * @param source command executioner
     * @param fileWriters writers, used to write captured data
     * @param regionCoordinates array that holds region_min_x, region_min_y, region_min_z, region_max_x, region_max_y,
     *                          region_max_z positions of region in world
     **/
    public static void renderRegionEntities(@NotNull CommandSourceStack source,
                                            @NotNull FileWriters fileWriters,
                                            int[] regionCoordinates){
        // Get level, where command is executed
        ServerLevel level = source.getLevel();

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
            DummyMultiBufferSource dummyMultiBufferSource = new DummyMultiBufferSource(
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
                    dummyMultiBufferSource,
                    entityRenderDispatcher.getPackedLightCoords(entity, minecraftConstant));
        }
    }

    /**
     * Renders world region.
     * @param source command executioner
     * @param fileWriters writers, used to write captured data
     * @param regionCoordinates coordinates of region to render
     * @see RenderCubeUtils#renderRegionEntities(CommandSourceStack, FileWriters, int[])
     **/
    public static void renderRegion(@NotNull CommandSourceStack source,
                                    @NotNull FileWriters fileWriters,
                                    int[] regionCoordinates){
        // Loop over coordinates included in region
        for(int x = regionCoordinates[0]; x <= regionCoordinates[3]; x++){
            for(int y = regionCoordinates[1]; y <= regionCoordinates[4]; y++){
                for(int z = regionCoordinates[2]; z <= regionCoordinates[5]; z++){
                    // Process cube
                    RenderCubeUtils.renderCube(
                            source,
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

        // Process region entities (result of this function determines final result of region rendering operation)
        RenderCubeUtils.renderRegionEntities(source, fileWriters, regionCoordinates);
    }
}
