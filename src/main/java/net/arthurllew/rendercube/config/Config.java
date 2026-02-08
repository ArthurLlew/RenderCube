package net.arthurllew.rendercube.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod configuration file.
 */
public class Config {
    /**
     * Config builder.
     */
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * Config instance.
     */
    public static final ModConfigSpec SPEC = BUILDER.build();

    /**
     * Max render distance value builder.
     */
    public static final ModConfigSpec.IntValue MAX_RENDER_DISTANCE = BUILDER
            .comment("Max allowed render distance")
            .defineInRange("maxRenderDistance", 400, 400, Integer.MAX_VALUE);
}
