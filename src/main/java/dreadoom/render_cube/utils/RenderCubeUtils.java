package dreadoom.render_cube.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import dreadoom.render_cube.RenderCube;
import dreadoom.render_cube.rendered_entities.RenderedCube;
import dreadoom.render_cube.rendered_entities.RenderedModel;
import dreadoom.render_cube.vertex_consumers.CommonVertexConsumer;
import dreadoom.render_cube.vertex_consumers.DummyMultiBufferSource;
import dreadoom.render_cube.vertex_consumers.LiquidVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
     * @param jsonWriter json sequence writer
     * @param levelPosition block position in level
     * @param regionPosition block position in region
     **/
    public static void renderCube(@NotNull CommandSourceStack source,
                                     @NotNull JsonSequenceWriter jsonWriter,
                                     @NotNull BlockPos levelPosition,
                                     @NotNull BlockPos regionPosition) throws IOException {
        // Get level, where command is executed
        ServerLevel level = source.getLevel();

        // Get BlockState at position
        BlockState block = level.getBlockState(levelPosition);

        // We do not want to render air
        if (!block.isAir()) {
            // Rendered models of different types
            RenderedModel renderedBlock = new RenderedModel();
            RenderedModel renderedEntity = new RenderedModel();
            RenderedModel renderedLiquid = new RenderedModel();

            // Init common vertex consumer
            CommonVertexConsumer commonVertexConsumer = new CommonVertexConsumer();

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

            // Convert consumed vertices to quads.
            commonVertexConsumer.convertVerticesToQuads();
            // Add all quads to block
            renderedBlock.quads.addAll(commonVertexConsumer.quads);

            // If this block contains fluid
            if (!block.getFluidState().isEmpty()){
                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer = new LiquidVertexConsumer(levelPosition);

                // Consume liquid vertices
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPosition,
                        level,
                        liquidVertexConsumer,
                        block,
                        block.getFluidState());

                // Convert consumed vertices to quads.
                liquidVertexConsumer.convertVerticesToQuads();
                // Add all quads to liquid
                renderedLiquid.quads.addAll(liquidVertexConsumer.quads);
            }

            // Get entity at our position
            BlockEntity entity = level.getBlockEntity(levelPosition);
            // If it is not null
            if(entity != null){
                // Create dummy MultiBufferSource
                DummyMultiBufferSource dummyMultiBufferSource = new DummyMultiBufferSource();

                // Render into dummy MultiBufferSource
                Minecraft.getInstance().getBlockEntityRenderDispatcher().render(
                        entity,
                        1.0F,
                        new PoseStack(),
                        dummyMultiBufferSource);

                // Convert consumed vertices to quads.
                dummyMultiBufferSource.buffer.convertVerticesToQuads();
                // Add all quads to entity
                renderedEntity.quads.addAll(dummyMultiBufferSource.buffer.quads);
            }

            // Init rendered cube, that will be written to disk
            RenderedCube renderedCube = new RenderedCube(
                    regionPosition.getX(),
                    regionPosition.getY(),
                    regionPosition.getZ(),
                    renderedBlock,
                    renderedEntity,
                    renderedLiquid);

            // Add cube to json
            jsonWriter.seqWriter.write(renderedCube);
        }
    }

    /**
     * Renders all entities in region.
     * @param source command executioner
     * @param jsonWriter json sequence writer
     * @param regionCoordinates array that holds region_min_x, region_min_y, region_min_z, region_max_x, region_max_y,
     *                          region_max_z positions of region in world
     **/
    //TODO: this methods works incorrectly
    public static void renderRegionEntities(@NotNull CommandSourceStack source,
                                               @NotNull JsonSequenceWriter jsonWriter,
                                               int[] regionCoordinates) throws IOException {
        // Get level, where command is executed
        ServerLevel level = source.getLevel();

        // Rendered models of different types
        RenderedModel renderedBlock = new RenderedModel();
        RenderedModel renderedEntity = new RenderedModel();
        RenderedModel renderedLiquid = new RenderedModel();

        // Get all entities in region
        List<Entity> entities = level.getEntities(
                (Entity)null, new AABB(
                        regionCoordinates[0],
                        regionCoordinates[1],
                        regionCoordinates[2],
                        regionCoordinates[3],
                        regionCoordinates[4],
                        regionCoordinates[5]),
                (entity) -> !(entity instanceof Player));

        // Process entities
        for (Entity entity: entities) {
            // Create dummy MultiBufferSource
            DummyMultiBufferSource dummyMultiBufferSource = new DummyMultiBufferSource();

            // Render into dummy MultiBufferSource
            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    entity,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0F,
                    1.0F,
                    new PoseStack(),
                    dummyMultiBufferSource,
                    OverlayTexture.NO_OVERLAY);

            // Convert consumed vertices to quads.
            dummyMultiBufferSource.buffer.convertVerticesToQuads();
            // Add all quads to entity
            renderedEntity.quads.addAll(dummyMultiBufferSource.buffer.quads);

            // Init rendered cube, that will be written to disk
            RenderedCube renderedCube = new RenderedCube(
                    entity.getX() - regionCoordinates[0],
                    entity.getY() - regionCoordinates[1],
                    entity.getZ() - regionCoordinates[2],
                    renderedBlock,
                    renderedEntity,
                    renderedLiquid);

            // Add cube to json
            jsonWriter.seqWriter.write(renderedCube);
        }
    }

    /**
     * Renders world region.
     * @see RenderCubeUtils#renderRegionEntities(CommandSourceStack, JsonSequenceWriter, int[])
     **/
    public static void renderRegion(@NotNull CommandSourceStack source,
                                       @NotNull JsonSequenceWriter jsonWriter,
                                       int[] regionCoordinates) throws IOException {
        // Loop over coordinates included in region
        for(int x = regionCoordinates[0]; x <= regionCoordinates[3]; x++){
            for(int y = regionCoordinates[1]; y <= regionCoordinates[4]; y++){
                for(int z = regionCoordinates[2]; z <= regionCoordinates[5]; z++){
                    // Process cube
                    RenderCubeUtils.renderCube(
                            source,
                            jsonWriter,
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
        RenderCubeUtils.renderRegionEntities(source, jsonWriter, regionCoordinates);
    }
}
