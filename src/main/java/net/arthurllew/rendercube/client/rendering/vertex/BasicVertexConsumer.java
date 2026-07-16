package net.arthurllew.rendercube.client.rendering.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Implements basic wrapper of {@link VertexConsumer}.
 */
public class BasicVertexConsumer implements VertexConsumer {
    /**
     * Holds file stream.
     */
    public OutputStream outputStream;

    /**
     * Vertex coordinates.
     */
    protected byte[] coordsBytes;
    /**
     * Vertex UVs.
     */
    protected byte[] uvBytes;
    /**
     * Vertex color.
     */
    protected byte[] colorBytes;
    /**
     * Vertex normal.
     */
    protected byte[] normalBytes;

    /**
     * Constructor.
     * @param fileStream opened file output stream where data will be saved
     */
    public BasicVertexConsumer(OutputStream fileStream) {
        outputStream = fileStream;
    }

    /**
     * Tries to finalize vertex data.
     */
    protected void tryToFinalizeVertex() {
        try {
            // If vertex is complete
            if (this.coordsBytes != null
                    && this.uvBytes != null
                    && this.colorBytes != null
                    && this.normalBytes != null) {
                // Dump vertex data
                outputStream.write(this.coordsBytes);
                outputStream.write(this.uvBytes);
                outputStream.write(this.colorBytes);
                outputStream.write(this.normalBytes);
                // Clear temporary vertex data
                this.coordsBytes = null;
                this.uvBytes = null;
                this.colorBytes = null;
                this.normalBytes = null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Copy of {@link VertexConsumer#putBulkData}. Includes control over rendering of ambient occlusion.
     */
    @Override
    public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float @NotNull [] brightness,
                            float red, float green, float blue, float alpha, int @NotNull [] lightmap,
                            int packedOverlay, boolean readAlpha) {
        int[] aint = quad.getVertices();
        Vec3i vec3i = quad.getDirection().getNormal();
        Matrix4f matrix4f = pose.pose();
        Vector3f vector3f = pose.transformNormal((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ(),
                new Vector3f());
        int j = aint.length / 8;
        int k = (int)(alpha * 255.0F);

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for(int l = 0; l < j; ++l) {
                intbuffer.clear();
                intbuffer.put(aint, l * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);

                float f3 = red;
                float f4 = green;
                float f5 = blue;

                // Brightness was moved from here below
                if (readAlpha) {
                    f3 *= (float)(bytebuffer.get(12) & 255);
                    f4 *= (float)(bytebuffer.get(13) & 255);
                    f5 *= (float)(bytebuffer.get(14) & 255);
                } else {
                    f3 *= 255.0F;
                    f4 *= 255.0F;
                    f5 *= 255.0F;
                }

                // Ambient occlusion depending on config
                if (Config.DATA.useMinecraftAmbientOcclusion) {
                    f3 *= brightness[l];
                    f4 *= brightness[l];
                    f5 *= brightness[l];
                }

                int i1 = FastColor.ARGB32.color(k, (int)f3, (int)f4, (int)f5);
                float f10 = bytebuffer.getFloat(16);
                float f9 = bytebuffer.getFloat(20);
                Vector3f vector3f1 = matrix4f.transformPosition(f, f1, f2, new Vector3f());
                this.addVertex(vector3f1.x(), vector3f1.y(), vector3f1.z(), i1, f10, f9, packedOverlay,
                        lightmap[l], vector3f.x(), vector3f.y(), vector3f.z());
            }
        }

    }

    /**
     * Captures vertex coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        // ByteBuffer for 3 doubles
        this.coordsBytes = ByteBuffer.allocate(3*Double.BYTES)
                .putDouble(x)
                .putDouble(y)
                .putDouble(z)
                .array();

        // Try to write vertex into file (vertex might be complete at this point)
        this.tryToFinalizeVertex();

        return this;
    }

    /**
     * Captures vertex color.
     * @param r Red channel
     * @param g Green channel
     * @param b Blue channel
     * @param a Alpha channel
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
        // ByteBuffer for 4 ints
        this.colorBytes = ByteBuffer.allocate(4*Integer.BYTES)
                .putInt(r)
                .putInt(g)
                .putInt(b)
                .putInt(a)
                .array();

        // Try to write vertex into file (vertex might be complete at this point)
        this.tryToFinalizeVertex();

        return this;
    }

    /**
     * Captures vertex UVs.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        // ByteBuffer for 2 floats
        this.uvBytes = ByteBuffer.allocate(2*Float.BYTES)
                .putFloat(u)
                .putFloat(v)
                .array();

        // Try to write vertex into file (vertex might be complete at this point)
        this.tryToFinalizeVertex();

        return this;
    }

    /**
     * Captures vertex normal vector.
     * @param x normal X coordinate
     * @param y normal Y coordinate
     * @param z normal Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setNormal(float x, float y, float z) {
        // ByteBuffer for 3 floats
        this.normalBytes = ByteBuffer.allocate(3*Float.BYTES)
                .putFloat(x)
                .putFloat(y)
                .putFloat(z)
                .array();

        // Try to write vertex into file (vertex might be complete at this point)
        this.tryToFinalizeVertex();

        return this;
    }

    /**
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setOverlay(int packedOverlay) {
        return this;
    }

    /**
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setUv1(int var1, int var2) {
        return this;
    }

    /**
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setUv2(int var1, int var2) {
        return this;
    }

    /**
     * Wraps this class in {@link MultiBufferSource}.
     * @return instance of {@link MultiBufferSource}.
     */
    public MultiBufferSource wrap() {
        return new FakeMultiBufferSource(this);
    }

    /**
     * Is used to capture geometry, produced by entity renderers.
     * @param buffer vertex consumer.
     */
    public record FakeMultiBufferSource(BasicVertexConsumer buffer) implements MultiBufferSource {
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
}
