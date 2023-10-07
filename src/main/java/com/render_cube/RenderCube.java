package com.render_cube;

import com.render_cube.utils.RenderCubeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static com.render_cube.utils.TextureAtlasDump.saveTextureAtlas;

@Mod(RenderCube.MODID)
public class RenderCube
{
    // The value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "rendercube";

    /**
     * Mod init.
     */
    public RenderCube()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Dumps all loaded texture atlases to separate files.
     * @throws IOException if file exceptions are encountered
     */
    public static void dumpTextureMaps() throws IOException {
        // Validate mod directory
        RenderCubeUtils.checkAndCreateModDir();

        // Gets minecraft texture manager
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        // Iterate over map of texture resources
        for (Map.Entry<ResourceLocation, AbstractTexture> entry : textureManager.byPath.entrySet()) {
            // Get value of map
            AbstractTexture textureObject = entry.getValue();

            // If it is texture atlas
            if (textureObject instanceof TextureAtlas textureAtlas) {
                // Get entry key as string. In this case this string will represent texture atlas resource location.
                String name = entry.getKey().toString();

                // Name of texture atlas is name of .png file
                name = name.substring(name.lastIndexOf("/") + 1);
                // without '.png'
                name = name.substring(0, name.lastIndexOf(".png"));

                // Save texture atlas
                saveTextureAtlas(name, textureAtlas.getId(), Paths.get(MODID));
            }
        }
    }
}
