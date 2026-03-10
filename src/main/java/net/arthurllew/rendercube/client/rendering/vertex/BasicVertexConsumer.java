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
 * Implements basic variables (store geometry data) and methods overrides of {@link VertexConsumer}.
 */
public class BasicVertexConsumer implements VertexConsumer {
    /**
     * Holds consumer file stream
     */
    public OutputStream outputStream;

    /**
     * Vertex coordinates data.
     */
    byte[] vertexBytes;
    /**
     * Vertex coordinates data.
     */
    byte[] uvBytes;
    /**
     * Vertex coordinates data.
     */
    byte[] colorBytes;

    /**
     * Constructs empty instance.
     * @param fileStream opened file output stream where data will be saved
     */
    public BasicVertexConsumer(OutputStream fileStream) {
        outputStream = fileStream;
    }

    /**
     * @return whether all required data is consumed.
     */
    private boolean allDataIsInPlace() {
        return this.vertexBytes != null && this.uvBytes != null && this.colorBytes != null;
    }

    /**
     * Tries to write data into file.
     */
    private void tryToWriteData() {
        try {
            if (this.allDataIsInPlace()) {
                outputStream.write(this.vertexBytes);
                outputStream.write(this.uvBytes);
                outputStream.write(this.colorBytes);
                this.vertexBytes = null;
                this.uvBytes = null;
                this.colorBytes = null;
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

                int vertexAlpha = readAlpha ?
                        (int)(alpha * (float)(bytebuffer.get(15) & 255) / 255.0F * 255.0F) : k;
                int i1 = FastColor.ARGB32.color(vertexAlpha, (int)f3, (int)f4, (int)f5);
                int j1 = this.applyBakedLighting(lightmap[l], bytebuffer);
                float f10 = bytebuffer.getFloat(16);
                float f9 = bytebuffer.getFloat(20);
                Vector3f vector3f1 = matrix4f.transformPosition(f, f1, f2, new Vector3f());
                this.applyBakedNormals(vector3f, bytebuffer, pose.normal());
                this.addVertex(vector3f1.x(), vector3f1.y(), vector3f1.z(), i1, f10, f9, packedOverlay,
                        j1, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }

    }

    /**
     * Saves vertex coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        // ByteBuffer size is 3 double (each is 8 bytes)
        this.vertexBytes = ByteBuffer.allocate(24)
                .putDouble(x)
                .putDouble(y)
                .putDouble(z)
                .array();

        // Try to write these bytes into file
        this.tryToWriteData();

        return this;
    }

    /**
     * Saves vertex color.
     * @param r Red channel
     * @param g Green channel
     * @param b Blue channel
     * @param a Alpha channel
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
        // ByteBuffer size is 4 ints (each is 4 bytes)
        this.colorBytes = ByteBuffer.allocate(16)
                .putInt(r)
                .putInt(g)
                .putInt(b)
                .putInt(a)
                .array();

        // Try to write these bytes into file
        this.tryToWriteData();

        return this;
    }

    /**
     * Saves vertex UVs.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        // ByteBuffer size is 2 floats (each is 4 bytes)
        this.uvBytes = ByteBuffer.allocate(8)
                .putFloat(u)
                .putFloat(v)
                .array();

        // Try to write these bytes into file
        this.tryToWriteData();

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
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setNormal(float x, float y, float z) {
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
