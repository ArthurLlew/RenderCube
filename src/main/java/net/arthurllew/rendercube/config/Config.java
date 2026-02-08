package net.arthurllew.rendercube.config;

import net.arthurllew.rendercube.RenderCube;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Mod configuration file.
 */
@Mod.EventBusSubscriber(modid = RenderCube.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    /**
     * Config builder.
     */
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    /**
     * Config instance.
     */
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    /**
     * Max render distance value builder.
     */
    private static final ForgeConfigSpec.IntValue MAX_RENDER_DISTANCE = BUILDER
            .comment("Max allowed render distance")
            .defineInRange("maxRenderDistance", 400, 400, Integer.MAX_VALUE);

    /**
     * Max render distance value.
     */
    public static int maxRenderDistance;

    /**
     * Config loading.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        maxRenderDistance = MAX_RENDER_DISTANCE.get();
    }
}
