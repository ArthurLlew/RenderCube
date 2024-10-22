package com.rendercube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

/**
 * Is used to capture geometry, produced by entity renderers.
 * @param buffer stored vertex consumer.
 */
public record FakeMultiBufferSource(BasicVertexConsumer buffer) implements MultiBufferSource {
    /**
     * Constructs instance from given {@link BasicVertexConsumer}.
     * @param buffer vertex consumer
     */
    public FakeMultiBufferSource {
    }

    /**
     * Returns stored instance of {@link BasicVertexConsumer} as {@link VertexConsumer}.
     * @param type object render type
     * @return instance of {@link VertexConsumer}
     */
    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        return buffer;
    }
}
