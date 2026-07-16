package net.arthurllew.rendercube.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.io.InputStream;
import java.util.*;

public class LODsColorResolver implements AutoCloseable {
    /**
     * Loaded images cache.
     */
    Map<ResourceLocation, NativeImage> imageCache = new LinkedHashMap<>(16, 1.0F);

    /**
     * Loaded images average color cache.
     */
    Map<NativeImage, QuadSpriteColor> imageColorCache = new LinkedHashMap<>(16, 1.0F);

    /**
     * @param level level where block is located
     * @param state block state
     * @param pos   block position
     * @return block color at given position in given level
     */
    public int getBlockColor(Level level, BlockState state, BlockPos pos) {
        return getQuadsAverageColor(getSpritesForDirection(level, state, pos, Direction.UP), state);
    }

    /**
     * @param level level where block is located
     * @param state block state
     * @param pos   block position
     * @param facing requested direction of block quads
     * @return either quad sprites facing requested direction or all quad sprites of the block model
     */
    @SuppressWarnings("SameParameterValue")
    protected List<QuadSprite> getSpritesForDirection(Level level, BlockState state, BlockPos pos, Direction facing) {
        // Block baked model
        BakedModel blockModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        // Compute list of quad sprites
        List<QuadSprite> quadSprites = new ArrayList<>();
        // If model is valid
        if (RenderShape.MODEL.equals(state.getRenderShape())) {
            // Try to collect quads facing requested direction
            List<BakedQuad> quads = blockModel.getQuads(state, facing, RandomSource.create());
            // If no quads face that direction collect all quads instead
            if (quads.isEmpty()) {
                quads = blockModel.getQuads(state, null, RandomSource.create());
            }
            // Is still not empty
            if (!quads.isEmpty()) {
                // Iterate quads
                for (BakedQuad quad : quads) {
                    // Try to collect quad sprite image and tint
                    NativeImage image = getImage(quad.getSprite());
                    if (image != null) {
                        quadSprites.add(new QuadSprite(image, getQuadTint(level, state, pos, quad)));
                    }
                }
            }
        }
        // Check fluid
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            // Try to get fluid handler
            FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluid.getType());
            if (handler != null) {
                // Fluid tint
                int fluidTint = handler.getFluidColor(level, pos, fluid);
                // Iterate fluid sprites
                for (TextureAtlasSprite fluidSprite : handler.getFluidSprites(level, pos, fluid)) {
                    // Try to collect sprite image and tint
                    NativeImage image = getImage(fluidSprite);
                    if (image != null) {
                        quadSprites.add(new QuadSprite(image, fluidTint));
                    }
                }
            }
        }
        // Return populated list (might be empty)
        return quadSprites;
    }

    /**
     * @return image corresponding to sprite
     */
    @SuppressWarnings("resource")
    protected NativeImage getImage(TextureAtlasSprite sprite) {
        // Ignore empty sprites
        if (sprite == null) {
            return null;
        }

        // Get sprite contents
        SpriteContents contents = sprite.contents();

        // Sprite resource location
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath(contents.name().getNamespace(),
                "textures/" + contents.name().getPath() + ".png");

        // Try to find this image in cache
        NativeImage cachedImage = this.imageCache.get(location);
        if (cachedImage != null) {
            return cachedImage;
        }

        // Try to obtain file stream
        Optional<Resource> resource = Minecraft.getInstance()
                .getResourceManager()
                .getResource(location);

        // If file not found in assets or active resource packs
        if (resource.isEmpty()) {
            return null;
        }

        // Try to read image from disk
        try (InputStream stream = resource.get().open()) {
            // Read, put to cache and return
            NativeImage image = NativeImage.read(stream);
            this.imageCache.put(location, image);
            return image;
        }
        catch (Exception ignored) {
            return null;
        }
    }

    /**
     * @param level level where block is located
     * @param state block state
     * @param pos   block position
     * @param quad  block quad
     * @return quad tint in RGB format
     */
    protected int getQuadTint(Level level, BlockState state, BlockPos pos, BakedQuad quad) {
        // Default color lookup or no tint
        return quad.isTinted() ?
                Minecraft.getInstance().getBlockColors().getColor(state, level, pos, quad.getTintIndex()) : -1;
    }

    /**
     * @param quadSprites quad sprites
     * @param state       block state
     * @return quad sprites average color of not opaque pixels
     */
    @SuppressWarnings("deprecation")
    protected int getQuadsAverageColor(List<QuadSprite> quadSprites, BlockState state) {
        // Skip empty list
        if (quadSprites.isEmpty()) {
            return 0;
        }

        // Color bins
        long r = 0, g = 0, b = 0;
        // Valid pixels count
        long pixelCount = 0;

        // Iterate sprites
        for (QuadSprite quadSprite : quadSprites) {
            // Prepare average color bins for this sprite
            long spriteMeanR = 0;
            long spriteMeanG = 0;
            long spriteMeanB = 0;

            // Valid pixels count
            long spritePixelCount = 0;

            // Try to find value in cache
            QuadSpriteColor cachedColor = this.imageColorCache.get(quadSprite.sprite);
            if (cachedColor != null) {
                // If sprite is not valid
                if (cachedColor.spritePixelCount == 0) {
                    // Skip it
                    continue;
                }

                // Fill average color with cached value
                spriteMeanR = cachedColor.spriteMeanR;
                spriteMeanG = cachedColor.spriteMeanG;
                spriteMeanB = cachedColor.spriteMeanB;
                // Flag that valid pixels were found
                spritePixelCount = cachedColor.spritePixelCount;
            }
            // Cache was empty
            else {
                // Iterate sprite pixels
                for (int ARGB : quadSprite.sprite.makePixelArray()) {
                    // Only non-opaque pixels count
                    if ((ARGB >>> 24 & 255) > 0) {
                        // Add pixel channels to average color
                        spriteMeanR += ARGB >> 16 & 255;
                        spriteMeanG += ARGB >> 8 & 255;
                        spriteMeanB += ARGB & 255;
                        // Update count
                        spritePixelCount++;
                    }
                }
            }

            // If at least one pixel was valid
            if (spritePixelCount > 0) {
                // Add color to cache
                this.imageColorCache.put(quadSprite.sprite,
                        new QuadSpriteColor(spritePixelCount, spriteMeanR, spriteMeanG, spriteMeanB));

                // If sprite has tint
                if (quadSprite.tintColor != -1) {
                    // Extract tint
                    int tintR = quadSprite.tintColor >> 16 & 255;
                    int tintG = quadSprite.tintColor >> 8 & 255;
                    int tintB = quadSprite.tintColor & 255;
                    // Multiply by it
                    spriteMeanR = (spriteMeanR * tintR + 127) / 255;
                    spriteMeanG = (spriteMeanG * tintG + 127) / 255;
                    spriteMeanB = (spriteMeanB * tintB + 127) / 255;
                }

                // Add to total color
                r += spriteMeanR;
                g += spriteMeanG;
                b += spriteMeanB;
                // Update sprite count
                pixelCount += spritePixelCount;
            }
            // Not a single pixel was valid
            else {
                // Put info about it to cache
                this.imageColorCache.put(quadSprite.sprite,
                        new QuadSpriteColor(0, 0, 0, 0));
            }
        }

        // Not a single pixel was valid
        if (pixelCount == 0) {
            return 0;
        }

        // Average color
        int avgR = (int) (r / pixelCount);
        int avgG = (int) (g / pixelCount);
        int avgB = (int) (b / pixelCount);

        // Adjust brightness for leaves
        if (state.getBlock() instanceof LeavesBlock) {
            avgR = Math.round(avgR * 0.8F);
            avgG = Math.round(avgG * 0.8F);
            avgB = Math.round(avgB * 0.8F);
        }

        // Combine color values
        return 0xFF000000 | (avgR << 16) | (avgG << 8) | avgB;
    }

    /**
     * Closes cached images.
     */
    @Override
    public void close() {
        for(NativeImage image : this.imageCache.values()) {
            image.close();
        }
    }

    /**
     * Stores quad sprite info.
     * @param sprite    quad sprite
     * @param tintColor quad sprite tint
     */
    protected record QuadSprite(NativeImage sprite, int tintColor){}

    /**
     * Stores quad sprite color.
     * @param spritePixelCount quad sprite pixel count
     * @param spriteMeanR      quad sprite average R color
     * @param spriteMeanG      quad sprite average R color
     * @param spriteMeanB      quad sprite average R color
     */
    protected record QuadSpriteColor(long spritePixelCount, long spriteMeanR, long spriteMeanG, long spriteMeanB){}
}
