package dreadoom.render_cube;

import dreadoom.render_cube.registry.RegistryHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static dreadoom.render_cube.utils.TextureAtlasDump.saveTextureAtlas;

@Mod(RenderCube.MODID)
public class RenderCube
{
    private static final Logger LOGGER = LogUtils.getLogger();

    // The value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "rendercube";

    /**
     * Indicates if player have reached main menu on startup.
     */
    private boolean titleScreenWasOpened = false;

    /**
     * Mod init.
     */
    public RenderCube()
    {
        // Register the setup method for mod loading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        // Registry setup
        RegistryHandler.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register event, that fires after game loading was complete
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL,
                                                                    false,
                                                                    FMLLoadCompleteEvent.class,
                                                                    (event) -> this.onLoadComplete());

        // Register event, that fires when main-menu screen is opened
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,
                                            false,
                                            ScreenOpenEvent.class,
                                            this::onMainMenuOpen);
    }

    /**
     * Is fires after game loading was complete. Sets up listener for resource-packs reload event.
     */
    // TODO: maybe refactor this method
    private void onLoadComplete() {
        // Register listener for resource-packs reload event, that will dump textures again
        FMLJavaModLoadingContext.get().getModEventBus().addListener(
                EventPriority.NORMAL,
                false,
                RegisterClientReloadListenersEvent.class,
                registerReloadListenerEvent -> registerReloadListenerEvent.registerReloadListener(
                        (ResourceManagerReloadListener) (resourceManager) -> {
                            // Only reload if we have ever opened title screen
                            if (titleScreenWasOpened) {
                                try {
                                    dumpTextureMaps();
                                } catch (IOException e) {
                                    LOGGER.error("Failed to dump texture maps with error.", e);
                                }
                            }})
        );
    }

    /**
     * Here at first user visit to main menu we dump textures and say, that we have visited main menu on startup.
     * @param event screen open event
     */
    // TODO: maybe refactor this method
    private void onMainMenuOpen(ScreenOpenEvent event) {
        if (!titleScreenWasOpened && event.getScreen() instanceof TitleScreen) {
            titleScreenWasOpened = true;
            try {
                dumpTextureMaps();
            } catch (IOException e) {
                LOGGER.error("Failed to dump texture maps with error.", e);
            }
        }
    }

    /**
     * Dumps all loaded texture atlases to separate files.
     * @throws IOException if file exceptions are encountered
     */
    private static void dumpTextureMaps() throws IOException {
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

    /**
     * Mode registry setup
     * @param event mod init event
     */
    private void setup(final FMLCommonSetupEvent event)
    {
        // Some pre-init code
        LOGGER.info("Starting setup");

        // Registry
        RegistryHandler.register();

        LOGGER.info("Setup successful");
    }
}
