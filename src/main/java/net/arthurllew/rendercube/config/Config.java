package net.arthurllew.rendercube.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.arthurllew.rendercube.RenderCube;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
         * Max render distance.
         */
        public int maxRenderDistance;

        /**
         * Controls rendering of Minecraft ambient occlusion.
         */
        public boolean useMinecraftAmbientOcclusion;

        /**
         * List of configured block writers.
         */
        public List<BlockConsumerConfig> blockConsumerConfigs;

        /**
         * Default data constructor.
         */
        private Data() {
            this.maxRenderDistance = 400;
            this.useMinecraftAmbientOcclusion = false;

            this.blockConsumerConfigs = List.of(
                    new BlockConsumerConfig(BlockConsumerSettingsType.EMISSION,
                            "15",
                            "renderedEmissive15",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.EMISSION,
                            "10",
                            "renderedEmissive10",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.EMISSION,
                            "5",
                            "renderedEmissive5",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.EMISSION,
                            "1",
                            "renderedEmissive1",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.LeavesBlock",
                            "renderedVegetation",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.BushBlock",
                            "renderedVegetation",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.VineBlock",
                            "renderedVegetation",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.CaveVines",
                            "renderedVegetation",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.ChestBlock",
                            "renderedChests",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.SignBlock",
                            "renderedSigns",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.BannerBlock",
                            "renderedBanners",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.BedBlock",
                            "renderedBeds",
                            true),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.DoorBlock",
                            "renderedDoors",
                            false),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.TrapDoorBlock",
                            "renderedDoors",
                            false));
        }

        /**
         * Applies data constraints.
         */
        private Data applyConstraints() {
            this.maxRenderDistance = Math.max(maxRenderDistance, 400);

            return this;
        }

        /**
         * Configured block writer. Contains writer type (class or block),
         * class string or block registry, filename for export and whether it
         * should cull sides.
         */
        public record BlockConsumerConfig(BlockConsumerSettingsType type, String entry,
                                          String filename, boolean cullSides) {

            /**
             * @return block consumer settings.
             */
            public @Nullable BlockConsumerSettings getSettings(@NotNull BlockState blockState) {
                switch (this.type) {
                    case CLASS:
                        // Try to handle custom writer
                        try {
                            // Get the Class object for the given class name
                            Class<?> targetClass = Class.forName(this.entry);

                            // Check if the block is an instance of that Class
                            if (targetClass.isInstance(blockState.getBlock())) {
                                // Return custom culling rule and file name
                                return new BlockConsumerSettings(this.cullSides, this.filename);
                            }
                            // Log incorrect config entry
                        } catch (ClassNotFoundException e) {
                            RenderCube.LOGGER.error("Class from config not found: {}", this.entry);
                        }

                        break;
                    case BLOCK:
                        // Try to handle custom writer
                        try {
                            // Check block is matching provided registry entry
                            if (blockState.is(BuiltInRegistries.BLOCK.get(
                                    new ResourceLocation(this.entry)))) {
                                // Return custom culling rule and file name
                                return new BlockConsumerSettings(this.cullSides, this.filename);
                            }
                            // Log incorrect config entry
                        } catch (IllegalStateException e) {
                            RenderCube.LOGGER.error("Block from config not found: {}", this.entry);
                        }

                        break;
                    case EMISSION:
                        // Try to handle custom writer
                        try {
                            // Check block has more than required emission
                            if (blockState.getLightEmission() >= Integer.parseInt(this.entry)) {
                                // Return custom culling rule and file name
                                return new BlockConsumerSettings(this.cullSides, this.filename);
                            }
                            // Log incorrect config entry
                        } catch (IllegalStateException e) {
                            RenderCube.LOGGER.error("Block with emission from config not found: {}", this.entry);
                        }

                        break;
                }

                return null;
            }

            /**
             * Record for customized block consumer settings.
             */
            public record BlockConsumerSettings(boolean cullSides, String filename) {}
        }

        /**
         * Defines what does the stored string might represent.
         */
        public enum BlockConsumerSettingsType {
            /**
             * Class name string.
             */
            CLASS,
            /**
             * Block registry string.
             */
            BLOCK,
            /**
             * String containing light level value.
             */
            EMISSION
        }
    }
}
