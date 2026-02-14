package net.arthurllew.rendercube.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod configuration file.
 */
public class Config
{
    /**
     * Config instance.
     */
    public static final Config CONFIG;

    /**
     * Config spec instance.
     */
    public static final ForgeConfigSpec CONFIG_SPEC;

    /**
     * Max render distance value builder.
     */
    public final ForgeConfigSpec.IntValue maxRenderDistance;

    /**
     * Config building.
     */
    private Config(ForgeConfigSpec.Builder builder) {
        maxRenderDistance = builder
                .comment("Max allowed render distance")
                .defineInRange("maxRenderDistance", 400, 400, Integer.MAX_VALUE);
    }

    // Config instances building
    static {
        Pair<Config, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Config::new);

        //Store the resulting values
        CONFIG = pair.getLeft();
        CONFIG_SPEC = pair.getRight();
    }
}
