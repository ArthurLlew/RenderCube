package net.arthurllew.rendercube.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.arthurllew.rendercube.RenderCube;

import java.io.*;
import java.util.List;

public class Config {
    /**
     * Gson object.
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Active config data.
     */
    public static Data DATA;

    /**
     * Inits config.
     */
    public static void initConfig() {
        // Config file
        File file = new File("config", RenderCube.MODID + ".json");

        // If config doesn't exist
        if (!file.exists()) {
            // Init with defaults
            createDefaultConfig();

            // Try to save config
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(DATA, writer);
            }
            // On error: add log
            catch (Exception e) {
                RenderCube.LOGGER.warn("Unable to save config file due to: ", e);
            }
        }
        // If config already exists
        else {
            // Try to read
            try (Reader reader = new FileReader(file)) {
                DATA = GSON.fromJson(reader, Data.class).applyConstraints();
            }
            // On error: set to defaults and add log
            catch (Exception e) {
                RenderCube.LOGGER.warn("Unable to read config file due to: ", e);
                createDefaultConfig();
            }
        }
    }

    /**
     * Inits config with defaults.
     */
    private static void createDefaultConfig() {
        DATA = new Data();
    }

    /**
     * Data holder.
     */
    public static class Data {
        /**
         *Max render distance.
         */
        public int maxRenderDistance;

        /**
         * List of custom writers. Each writer contains class name, filename for export and whether it
         * should cull sides.
         */
        public List<CustomWriter> customWriters;

        /**
         * Default data constructor.
         */
        private Data() {
            this.maxRenderDistance = 400;

            this.customWriters = List.of(
                    new CustomWriter("net.minecraft.world.level.block.LeavesBlock",
                            "renderedVegetation",
                            true),
                    new CustomWriter("net.minecraft.world.level.block.BushBlock",
                            "renderedVegetation",
                            true),
                    new CustomWriter("net.minecraft.world.level.block.VineBlock",
                            "renderedVegetation",
                            true));
        }

        /**
         * Applies data constraints.
         */
        private Data applyConstraints() {
            this.maxRenderDistance = Math.max(maxRenderDistance, 400);

            return this;
        }

        /**
         * Stores custom writer data.
         */
        public record CustomWriter(String className, String filename, boolean checkSides) {
        }
    }
}
