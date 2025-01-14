package net.arthurllew.rendercube;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderCube implements ModInitializer {
	public static final String MODID = "rendercube";

    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	/**
	 * Mod texture atlases directory.
	 */
	public static final String TEXTURE_ATLASES_DIR = MODID + "\\" + "texture_atlases";

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("RenderCube init");
	}
}