package net.arthurllew.rendercube.mod.littletiles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.arthurllew.rendercube.client.rendering.vertex.CommonVertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.ModList;
import team.creative.creativecore.client.render.box.QuadGeneratorContext;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.AlignedBox;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.math.box.LittleBox;

import java.util.Iterator;
import java.util.List;

public class LittleTilesManager {
    /**
     * Little tiles mod id.
     */
    private static final String MODID = "littletiles";
    /**
     * Whether little tiles mod is installed.
     */
    private static final boolean INSTALLED = ModList.get().isLoaded(MODID);

    /**
     * Renders little tiles into provided vertex consumer.
     * @param blockEntity little tiles entity
     * @param level level
     * @param pos block position
     * @param vertexConsumer vertex consumer
     */
    public static void render(BlockEntity blockEntity, LevelAccessor level, BlockPos pos,
                              CommonVertexConsumer vertexConsumer) {
        if (INSTALLED && blockEntity instanceof BETiles tiles) {
            // Get grid resolution for this block
            float gridRes = tiles.getGrid().count;

            // For every collection of tiles matching one reference block
            for (Pair<IParentCollection, LittleTile> tile : tiles.allTiles()) {
                // Little tiles quad generation context
                QuadGeneratorContext context = new QuadGeneratorContext();

                // Get reference block state and its model data at current location
                BlockState state = tile.getValue().getState();
                BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
                ModelData blockModelData = blockModel.getModelData(level, pos, state, ModelData.EMPTY);
                RandomSource randomSource = RandomSource.create(state.getSeed(pos));

                // Modified tile color
                int color = tile.getValue().color;

                // Cycle over each little tile (iterator will cycle back, so some other condition is required)
                Iterator<LittleBox> iter = tile.getValue().iterator();
                int i = 0;
                while(iter.hasNext() && i < tile.getValue().size()) {
                    // Form a box matching current little tile
                    LittleBox box = iter.next();
                    LittleRenderBox renderBox = new LittleRenderBox(
                            new AlignedBox(box.minX / gridRes, box.minY / gridRes, box.minZ / gridRes,
                                    box.maxX / gridRes, box.maxY / gridRes, box.maxZ / gridRes));

                    // For every render type there is in model and for every direction
                    for (RenderType rendertype : blockModel.getRenderTypes(state, randomSource, blockModelData)) {
                        for (Facing facing : Facing.VALUES) {
                            // Get model quads (faces)
                            List<BakedQuad> quads = renderBox.getBakedQuad(context, level, pos,
                                    new BlockPos(0,0,0),
                                    state, blockModel, blockModelData, facing, rendertype, randomSource,
                                    false, 0);

                            // Put vertex data into consumer
                            for (BakedQuad quad : quads) {
                                vertexConsumer.putBulkData(new PoseStack().last(), quad,
                                        ((color >> 16) & 0xFF) / 256f,
                                        ((color >> 8) & 0xFF) / 256f,
                                        (color & 0xFF) / 256f,
                                        0, 0);
                            }
                        }
                    }

                    i++;
                }
            }
        }
    }
}
