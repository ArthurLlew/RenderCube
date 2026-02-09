package net.arthurllew.rendercube;

import net.arthurllew.rendercube.config.Config;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderCube implements ModInitializer {
	public static final String MODID = "rendercube";

	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		// Init config file
		Config.initConfig();
	}
}