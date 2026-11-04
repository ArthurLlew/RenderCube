package net.arthurllew.rendercube.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.arthurllew.rendercube.RenderCube;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Config {
    /**
     * Config file path.
     */
    private static final File FILE = new File("config", RenderCube.MODID + ".json");
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
        // If config doesn't exist
        if (!FILE.exists()) {
            // Init with defaults
            initDefaultConfig();
            // Try to save
            tryToSaveConfig();
        }
        // If config already exists
        else {
            // Try to read
            try (Reader reader = new FileReader(FILE)) {
                DATA = GSON.fromJson(reader, Data.class).applyConstraints();
            }
            // On error: add log init with defaults
            catch (Exception e) {
                RenderCube.LOGGER.warn("Unable to read config file due to: ", e);
                initDefaultConfig();
            }
        }
    }

    /**
     * Inits config with defaults.
     */
    private static void initDefaultConfig() {
        DATA = new Data();
    }

    /**
     * Tries to save config into file.
     */
    public static void tryToSaveConfig() {
        // Try to save config
        try (Writer writer = new FileWriter(FILE)) {
            GSON.toJson(DATA, writer);
        }
        // On error: add log
        catch (Exception e) {
            RenderCube.LOGGER.warn("Unable to save config file due to: ", e);
        }
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
         * Controls per-chunk vertex data saving.
         */
        public boolean perChunkRendering;
        /**
         * Controls capture of directional lighting (each block has a pre-defined shading applied
         * to a quad depending on its direction, which allows the thing to look less flat).
         */
        public boolean useDirectionalShading;
        /**
         * Render region border culling flags (a quad won't be culled, if it is on region border,
         * is facing out of render region and has direction that is set to on in this map).
         */
        public Map<Direction, Boolean> regionBorderUnculledDirections;
        /**
         * List of configured block writers.
         */
        public List<BlockConsumerConfig> blockConsumerConfigs;

        /**
         * Default data constructor.
         */
        private Data() {
            this.maxRenderDistance = 400;
            // Per-chunk split should be seen as a PRO option
            this.perChunkRendering = false;
            // Directional lighting is a bonus
            this.useDirectionalShading = false;
            // Culling is in normal mode by default (all flags are off)
            this.regionBorderUnculledDirections = new EnumMap<>(Direction.class);

            // Default filters for blocks
            this.blockConsumerConfigs = new ArrayList<>(List.of(
                    // Emissive blocks
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
                    // Common vegetation
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
                    // Chests
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.ChestBlock",
                            "renderedChests",
                            true),
                    // Signs
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.SignBlock",
                            "renderedSigns",
                            true),
                    // Banners
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.BannerBlock",
                            "renderedBanners",
                            true),
                    // Beds
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.BedBlock",
                            "renderedBeds",
                            true),
                    // Door-like
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.DoorBlock",
                            "renderedDoors",
                            false),
                    new BlockConsumerConfig(BlockConsumerSettingsType.CLASS,
                            "net.minecraft.world.level.block.TrapDoorBlock",
                            "renderedDoors",
                            false)));
        }

        /**
         * Copy constructor.
         */
        public Data(Data other) {
            this.maxRenderDistance = other.maxRenderDistance;
            this.perChunkRendering = other.perChunkRendering;
            this.useDirectionalShading = other.useDirectionalShading;
            this.regionBorderUnculledDirections = new EnumMap<>(Direction.class);
            for (Direction d : Direction.values()) {
                Boolean v = other.regionBorderUnculledDirections.get(d);
                this.regionBorderUnculledDirections.put(d, v != null && v);
            }
            this.blockConsumerConfigs = new ArrayList<>(other.blockConsumerConfigs.size());
            for (BlockConsumerConfig c : other.blockConsumerConfigs) {
                this.blockConsumerConfigs.add(new BlockConsumerConfig(c));
            }
        }

        /**
         * Applies data constraints.
         */
        private Data applyConstraints() {
            // Min region cap is 400
            this.maxRenderDistance = Math.max(maxRenderDistance, 400);

            // Gson gives a LinkedHashMap (or null): rebuild a full EnumMap with defaults for missing directions
            Map<Direction, Boolean> validated = new EnumMap<>(Direction.class);
            for (Direction d : Direction.values()) {
                Boolean v = (this.regionBorderUnculledDirections != null)
                            ? this.regionBorderUnculledDirections.get(d) : null;
                // Default for a missing direction
                validated.put(d, (v != null) && v);
            }
            this.regionBorderUnculledDirections = validated;

            return this;
        }

        /**
         * Contains block filter settings. Is used to filter blocks and set where to save them and
         * whether to cull their sides (or ignore culling to allow blocks like doors to be opened in
         * 3D software in a natural way).
         */
        public static class BlockConsumerConfig {
            /**
             * Filter type.
             */
            public BlockConsumerSettingsType type;
            /**
             * Filter entry.
             */
            public String entry;
            /**
             * Filter file name.
             */
            public String filename;
            /**
             * Filter culling rule.
             */
            public boolean cullSides;

            /**
             * Constructor.
             */
            BlockConsumerConfig(BlockConsumerSettingsType type, String entry,
                                String filename, boolean cullSides) {
                this.type = type;
                this.entry = entry;
                this.filename = filename;
                this.cullSides = cullSides;
            }

            /**
             * Copy constructor.
             */
            BlockConsumerConfig(BlockConsumerConfig other) {
                this.type = other.type;
                this.entry = other.entry;
                this.filename = other.filename;
                this.cullSides = other.cullSides;
            }

            /**
             * @return block consumer settings.
             */
            public @Nullable BlockConsumerSettings getSettings(@NotNull Level level,
                                                               @NotNull BlockState blockState,
                                                               @NotNull BlockPos levelPos) {
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
                                    ResourceLocation.parse(this.entry)))) {
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
                            if (blockState.getLightEmission(level, levelPos) >= Integer.parseInt(this.entry)) {
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
         * Defines what does the block consumer settings string might represent.
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
