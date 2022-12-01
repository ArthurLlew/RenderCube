package com.shermobeaver.render_cube.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shermobeaver.render_cube.RenderCube;
import com.shermobeaver.render_cube.rendered_entities.RenderedBlock;
import com.shermobeaver.render_cube.rendered_entities.RenderedQuad;
import com.shermobeaver.render_cube.vertex_consumers.CommonVertexConsumer;
import com.shermobeaver.render_cube.vertex_consumers.DummyMultiBufferSource;
import com.shermobeaver.render_cube.vertex_consumers.LiquidVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
     * Renders one block.
     * @param source command executioner
     * @param jsonWriter json sequence writer
     * @param levelPosition block position in level
     * @param regionPosition block position in region
     **/
    public static boolean renderBlock(@NotNull CommandSourceStack source,
                                      @NotNull JsonSequenceWriter jsonWriter,
                                      @NotNull BlockPos levelPosition,
                                      @NotNull BlockPos regionPosition){
        try{
            // Get level, where command is executed
            ServerLevel level = source.getLevel();

            // Get BlockState at position
            BlockState block = level.getBlockState(levelPosition);

            // We do not want to render air
            if (!block.isAir()) {
                // Rendered block
                RenderedBlock renderedBlock = new RenderedBlock(
                        regionPosition.getX(),
                        regionPosition.getY(),
                        regionPosition.getZ());

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

                // There might be extra vertices, that we have consumed. We try to convert them to quads.
                commonVertexConsumer.convertVerticesToQuads();
                // Add all quads to block
                renderedBlock.quads.addAll(commonVertexConsumer.quads);

                // Create dummy MultiBufferSource
                DummyMultiBufferSource dummyMultiBufferSource = new DummyMultiBufferSource();
                // Get entity at out position
                BlockEntity entity = level.getBlockEntity(levelPosition);
                // If it is not null
                if(entity != null){
                    // Get its renderer
                    BlockEntityRenderer<BlockEntity> renderer = Minecraft.
                            getInstance().getBlockEntityRenderDispatcher().getRenderer(entity);
                    // If it is not null
                    if(renderer != null){
                        // Render into dummy MultiBufferSource
                        renderer.render(
                                entity,
                                1.0F,
                                new PoseStack(),
                                dummyMultiBufferSource,
                                15728880,
                                OverlayTexture.NO_OVERLAY);

                        // Convert entity vertices to quads
                        dummyMultiBufferSource.buffer.convertVerticesToQuads();
                        // Add all quads to block
                        renderedBlock.quads.addAll(dummyMultiBufferSource.buffer.quads);
                    }
                }

                // Init liquid consumer
                LiquidVertexConsumer liquidVertexConsumer = new LiquidVertexConsumer(levelPosition);

                // Consume liquid vertices
                Minecraft.getInstance().getBlockRenderer().renderLiquid(
                        levelPosition,
                        level,
                        liquidVertexConsumer,
                        block,
                        block.getFluidState());

                // Convert liquid vertices to quads
                liquidVertexConsumer.convertVerticesToQuads();
                // Add all quads to block
                renderedBlock.quads.addAll(liquidVertexConsumer.quads);

                // Add block to json
                jsonWriter.seqWriter.write(renderedBlock);
            }

            // Operation was successful
            return true;

        } catch( Exception e) {
            // Notify about exception
            source.sendFailure(new TextComponent("renderBlock: " + e));

            // We encountered errors
            return false;
        }
    }

    /**
     * Is used to process quad list.
     * @param level minecraft level, where block is situated
     * @param block block to render
     * @param position block position in level
     * @param quads quads to process
     **/
    private static void processQuads(@NotNull ServerLevel level,
                                     @NotNull BlockState block,
                                     @NotNull BlockPos position,
                                     @NotNull List<BakedQuad> quads,
                                     @NotNull RenderedBlock renderedBlock){
        // For each quad
        for (BakedQuad quad : quads) {
            // If we should render this face
            if (Block.shouldRenderFace(block, level, position, quad.getDirection(),
                    position.mutable().setWithOffset(position, quad.getDirection()))){
                // Create rendered quad from this quad
                RenderedQuad renderedQuad = new RenderedQuad(quad);

                // Add quad to block
                renderedBlock.quads.add(renderedQuad);
            }
        }
    }
}
