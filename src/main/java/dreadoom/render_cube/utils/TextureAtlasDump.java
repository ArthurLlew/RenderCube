package dreadoom.render_cube.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;

import net.minecraft.util.Mth;
import net.minecraftforge.fml.StartupMessageManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

/**
 * Manages texture dumping.
 */
public class TextureAtlasDump {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Saves provided texture atlas as png
     * @param name name of atlas, that is used to name file
     * @param textureId id of texture atlas
     * @param modDir directory, where file will be stored
     * @throws IOException file errors are not caught
     */
    public static void saveTextureAtlas(String name, int textureId, Path modDir) throws IOException {
        // GL init
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

        // Get parent sizes
        int parentTextureWidth = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int parentTextureHeight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);

        // minimal of width and height
        int minimumSize = Math.min(parentTextureWidth, parentTextureHeight);

        // Amount of quality levels of image
        int mipmapLevels = Mth.log2(minimumSize);

        // Message on startup about this action
        StartupMessageManager.addModMessage(String.format("Dumping TextureMap textures to file: %s", name));

        // If this atlas has quality levels count < 1, then we do nothing
        if (mipmapLevels < 1)
            return;

        // We are interested in image of the highest quality
        int level = 0;

        // Image sizes again
        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, level, GL11.GL_TEXTURE_HEIGHT);

        // Total pixels
        int size = width * height;

        // If image is suddenly empty
        if (size == 0)
            return;

        // Init buffered image
        BufferedImage bufferedimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Path where file will be stored
        Path output = modDir.resolve(name + "_mipmap_0.png");

        // Buffer for pixels RGBA
        IntBuffer buffer = BufferUtils.createIntBuffer(size);
        // Array of pixels RGBA
        int[] data = new int[size];

        // Write pixels to buffer
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, level, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, buffer);
        buffer.get(data);
        bufferedimage.setRGB(0, 0, width, height, data, 0, width);

        // Save buffer as png image
        ImageIO.write(bufferedimage, "png", output.toFile());

        // Log about success
        LOGGER.info("Exported atlas to: {}", output.toString());
    }
}
