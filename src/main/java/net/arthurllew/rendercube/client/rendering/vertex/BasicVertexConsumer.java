package net.arthurllew.rendercube.client.rendering.vertex;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.arthurllew.rendercube.config.Config;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
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
    protected double[] coordsBytes = new double[3];
    /**
     * Vertex UVs.
     */
    protected float[] uvBytes = new float[2];
    /**
     * Vertex color.
     */
    protected int[] colorBytes = new int[4];
    /**
     * Vertex normal.
     */
    protected float[] normalBytes = new float[3];

    /**
     * Constructor.
     * @param fileStream opened file output stream where data will be saved
     */
    public BasicVertexConsumer(OutputStream fileStream) {
        outputStream = fileStream;
    }

    /**
     * Copy of {@link VertexConsumer#putBulkData}. Includes control over rendering of ambient occlusion.
     */
    @Override
    public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls,
                            float red, float green, float blue, int[] combinedLights,
                            int combinedOverlay, boolean mulColor) {
        float[] fs = new float[]{colorMuls[0], colorMuls[1], colorMuls[2], colorMuls[3]};
        int[] is = new int[]{combinedLights[0], combinedLights[1], combinedLights[2], combinedLights[3]};
        int[] js = quad.getVertices();
        Vec3i vec3i = quad.getDirection().getNormal();
        Matrix4f matrix4f = poseEntry.pose();
        Vector3f vector3f = poseEntry.normal().transform(
                new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ()));
        int j = js.length / 8;

        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = memoryStack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intBuffer = byteBuffer.asIntBuffer();

            for(int k = 0; k < j; ++k) {
                intBuffer.clear();
                intBuffer.put(js, k * 8, 8);
                float f = byteBuffer.getFloat(0);
                float g = byteBuffer.getFloat(4);
                float h = byteBuffer.getFloat(8);

                float o = red;
                float p = green;
                float q = blue;

                // Brightness was moved from here below
                if (mulColor) {
                    o *= (float)(byteBuffer.get(12) & 255) / 255.0F;
                    p *= (float)(byteBuffer.get(13) & 255) / 255.0F;
                    q *= (float)(byteBuffer.get(14) & 255) / 255.0F;
                }

                // Ambient occlusion depending on config
                if (Config.DATA.useMinecraftAmbientOcclusion) {
                    o *= fs[k];
                    p *= fs[k];
                    q *= fs[k];
                }

                int r = is[k];
                float m = byteBuffer.getFloat(16);
                float n = byteBuffer.getFloat(20);
                Vector4f vector4f = matrix4f.transform(new Vector4f(f, g, h, 1.0F));
                this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), o, p, q, 1.0F, m, n,
                        combinedOverlay, r, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }

    }

    /**
     * Captures full vertex data.
     * @param x X vertex coordinate
     * @param y Y vertex coordinate
     * @param z Z vertex coordinate
     * @param r R channel
     * @param g G channel
     * @param b B channel
     * @param a A channel
     * @param u U vertex coordinate
     * @param v V vertex coordinate
     * @param overlayCords idk
     * @param uv2 idk
     * @param normal_x X normal vector coordinate
     * @param normal_y Y normal vector coordinate
     * @param normal_z Z normal vector coordinate
     */
    @Override
    public void vertex(float x, float y, float z,
                       float r, float g, float b, float a,
                       float u, float v,
                       int overlayCords, int uv2,
                       float normal_x, float normal_y, float normal_z) {
        this.vertex(x, y, z);
        this.uv(u, v);
        this.color(r, g, b, a);
        this.endVertex();
    }

    /**
     * Captures vertex coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        coordsBytes[0] = x;
        coordsBytes[1] = y;
        coordsBytes[2] = z;
        return this;
    }

    /**
     * Captures vertex UVs.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer uv(float u, float v) {
        uvBytes[0] = u;
        uvBytes[1] = v;
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
    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        colorBytes[0] = r;
        colorBytes[1] = g;
        colorBytes[2] = b;
        colorBytes[3] = a;
        return this;
    }

    /**
     * Does nothing.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer normal(float x, float y, float z) {
        normalBytes[0] = x;
        normalBytes[1] = y;
        normalBytes[2] = z;
        return this;
    }

    /**
     * Tries to finalize vertex data.
     */
    @Override
    public void endVertex() {
        // ByteBuffer size is 3 double (each is 8 bytes) + 2 floats (each is 4 bytes)
        //  + 4 ints (each is 4 bytes) + 3 floats (each is 4 bytes)
        byte[] bytes = ByteBuffer.allocate(60)
                .putDouble(coordsBytes[0]).putDouble(coordsBytes[1])
                .putDouble(coordsBytes[2])
                .putFloat(uvBytes[0]).putFloat(uvBytes[1])
                .putInt(colorBytes[0]).putInt(colorBytes[1])
                .putInt(colorBytes[2]).putInt(colorBytes[3])
                .putFloat(normalBytes[0]).putFloat(normalBytes[1]).putFloat(normalBytes[2])
                .array();

        // Try to write these bytes into file
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does nothing.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer overlayCoords(int u, int v) {
        return this;
    }

    /**
     * Does nothing.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer uv2(int u, int v) {
        return this;
    }

    /**
     * Does nothing.
     * @param r Red channel
     * @param g Green channel
     * @param b Blue channel
     * @param a Alpha channel
     */
    @Override
    public void defaultColor(int r, int g, int b, int a) {
    }

    /**
     * Does nothing.
     */
    @Override
    public void unsetDefaultColor() {
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
