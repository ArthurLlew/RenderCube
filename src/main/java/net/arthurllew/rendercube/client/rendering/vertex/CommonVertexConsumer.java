package net.arthurllew.rendercube.client.rendering.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;

/**
 * Used to capture vertex data, produced by renderers.
 */
public class CommonVertexConsumer extends BasicVertexConsumer {
    /**
     * Position in render region.
     */
    private final BlockPos regionPos;

    /**
     * Constructor.
     * @param fileStream opened file output stream where data will be saved
     * @param regionPos  position inside render region
     */
    public CommonVertexConsumer(OutputStream fileStream, BlockPos regionPos) {
        super(fileStream);

        this.regionPos = regionPos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        super.vertex(regionPos.getX() + x, regionPos.getY() + y, regionPos.getZ() + z);
        return this;
    }
}
