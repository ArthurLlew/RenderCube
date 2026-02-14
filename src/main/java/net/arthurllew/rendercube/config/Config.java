package net.arthurllew.rendercube.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod configuration file.
 */
public class Config {
    /**
     * Config instance.
     */
    public static final Config CONFIG;

    /**
     * Config spec instance.
     */
    public static final ModConfigSpec CONFIG_SPEC;

    /**
     * Max render distance value builder.
     */
    public final ModConfigSpec.IntValue maxRenderDistance;

    /**
     * Config building.
     */
    private Config(ModConfigSpec.Builder builder) {
        maxRenderDistance = builder
                .comment("Max allowed render distance")
                .defineInRange("maxRenderDistance", 400, 400, Integer.MAX_VALUE);
    }

    // Config instances building
    static {
        Pair<Config, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Config::new);

        //Store the resulting values
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
