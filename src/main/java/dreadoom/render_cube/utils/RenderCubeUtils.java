package dreadoom.render_cube.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.rendered_geometry.RenderedQuad;
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
     * @param jsonWriters writers, used to write captured data
     * @param levelPosition block position in level
     * @param regionPosition block position in region
     * @throws IOException when file exceptions are encountered
     **/
    public static void renderCube(@NotNull CommandSourceStack source,
                                  @NotNull JsonWriters jsonWriters,
                                  @NotNull BlockPos levelPosition,
                                  @NotNull BlockPos regionPosition) throws IOException {
        // Get level, where command is executed
        ServerLevel level = source.getLevel();

        // Get BlockState at position
        BlockState block = level.getBlockState(levelPosition);

        // We do not want to render air
        if (!block.isAir()) {
            // Init common vertex consumer
            CommonVertexConsumer commonVertexConsumer = new CommonVertexConsumer(regionPosition);

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

            // Convert consumed vertices to quads and write them one by one
            for (RenderedQuad quad: commonVertexConsumer.convertVerticesToQuads()) {
                jsonWriters.blockWriter.seqWriter.write(quad);
            }

            // If this block contains fluid
            if (!block.getFluidState().isEmpty()){
                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer = new LiquidVertexConsumer(regionPosition, levelPosition);

                // Consume liquid vertices
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPosition,
                        level,
                        liquidVertexConsumer,
                        block,
                        block.getFluidState());

                // Convert consumed vertices to quads and write them one by one
                for (RenderedQuad quad: liquidVertexConsumer.convertVerticesToQuads()) {
                    jsonWriters.liquidWriter.seqWriter.write(quad);
                }
            }

            // Get entity at our position
            BlockEntity entity = level.getBlockEntity(levelPosition);
            // If it is not null
            if(entity != null){
                // Create dummy MultiBufferSource
                DummyMultiBufferSource dummyMultiBufferSource =
                        new DummyMultiBufferSource(new CommonVertexConsumer(regionPosition));

                // Render into dummy MultiBufferSource
                Minecraft.getInstance().getBlockEntityRenderDispatcher().render(
                        entity,
                        1.0F,
                        new PoseStack(),
                        dummyMultiBufferSource);

                // Convert consumed vertices to quads and write them one by one
                for (RenderedQuad quad: dummyMultiBufferSource.getBuffer().convertVerticesToQuads()) {
                    jsonWriters.blockEntityWriter.seqWriter.write(quad);
                }
            }
        }
    }

    /**
     * Renders all entities in region.
     * @param source command executioner
     * @param jsonWriters writers, used to write captured data
     * @param regionCoordinates array that holds region_min_x, region_min_y, region_min_z, region_max_x, region_max_y,
     *                          region_max_z positions of region in world
     * @throws IOException when file exceptions are encountered
     **/
    public static void renderRegionEntities(@NotNull CommandSourceStack source,
                                            @NotNull JsonWriters jsonWriters,
                                            int[] regionCoordinates) throws IOException{
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
            DummyMultiBufferSource dummyMultiBufferSource = new DummyMultiBufferSource(new BasicVertexConsumer());

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

            // Convert consumed vertices to quads and write them one by one
            for (RenderedQuad quad: dummyMultiBufferSource.getBuffer().convertVerticesToQuads()) {
                jsonWriters.entityWriter.seqWriter.write(quad);
            }
        }
    }

    /**
     * Renders world region.
     * @param source command executioner
     * @param jsonWriters writers, used to write captured data
     * @param regionCoordinates coordinates of region to render
     * @see RenderCubeUtils#renderRegionEntities(CommandSourceStack, JsonWriters, int[])
     * @throws IOException when file exceptions are encountered
     **/
    public static void renderRegion(@NotNull CommandSourceStack source,
                                    @NotNull JsonWriters jsonWriters,
                                    int[] regionCoordinates) throws IOException{
        // Loop over coordinates included in region
        for(int x = regionCoordinates[0]; x <= regionCoordinates[3]; x++){
            for(int y = regionCoordinates[1]; y <= regionCoordinates[4]; y++){
                for(int z = regionCoordinates[2]; z <= regionCoordinates[5]; z++){
                    // Process cube
                    RenderCubeUtils.renderCube(
                            source,
                            jsonWriters,
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
        RenderCubeUtils.renderRegionEntities(source, jsonWriters, regionCoordinates);
    }
}
