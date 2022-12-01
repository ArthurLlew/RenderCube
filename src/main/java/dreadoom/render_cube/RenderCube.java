package dreadoom.render_cube;

import dreadoom.render_cube.registry.RegistryHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
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
import java.util.Collection;

import static dreadoom.render_cube.utils.TextureAtlasDump.saveTextureAtlas;

@Mod(RenderCube.MODID)
public class RenderCube
{
    private static final Logger LOGGER = LogUtils.getLogger();

    // The value here should match an entry in the META-INF/mods.toml file
    public static final String MODID = "rendercube";

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
     * Is fired after first load or resource-pack reload. If we have visited main menu screen we also dump textures if
     * this event has fired.
     */
    private void onLoadComplete() {
        // Reload when game resources change
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

    private static void dumpTextureMaps() throws IOException {
        TextureAtlas textureAtlas = null;
        try{
            TextureManager textureManager = Minecraft.getInstance().getTextureManager();

            Collection<ResourceLocation> resourceLocations = Minecraft.getInstance().getResourceManager().listResources(
                    "textures/atlas",
                    res -> res.chars().noneMatch(i -> Character.isLetter(i) && Character.isUpperCase(i)));

            if (!resourceLocations.isEmpty()){
                for (ResourceLocation resource: resourceLocations) {
                    LOGGER.debug("resource: " + resource.toString());
                }
            }

            textureAtlas = (TextureAtlas) textureManager.getTexture(new ResourceLocation("textures/atlas/blocks.png"));
        }
        catch (Exception e){
            // Notify about exception
            LOGGER.error("texture atlas", e);
        }
        try{
            if (textureAtlas != null)
                saveTextureAtlas("blocks", textureAtlas.getId(), Paths.get(MODID));
        }
        catch (Exception e){
            // Notify about exception
            LOGGER.error("texture atlas", e);
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
