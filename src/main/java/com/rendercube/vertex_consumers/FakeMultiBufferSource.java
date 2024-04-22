package com.rendercube.vertex_consumers;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Is used to capture geometry, produced by entity renderers.
 * @param buffer stored vertex consumer.
 */
public record FakeMultiBufferSource(BasicVertexConsumer buffer) implements VertexConsumerProvider {
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
    public @NotNull VertexConsumer getBuffer(@NotNull RenderLayer type) {
        return buffer;
    }
}
