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
 * Implements basic variables (store geometry data) and methods overrides of {@link VertexConsumer}.
 */
public class BasicVertexConsumer implements VertexConsumer {
    /**
     * Holds consumer file stream
     */
    public OutputStream outputStream;

    /**
     * Saved vertex coordinates.
     */
    private final double[] savedVertexCoordinates = new double[3];

    /**
     * Saved vertex UV coordinates.
     */
    private final float[] savedVertexUVs = new float[2];

    /**
     * Saved vertex color as hex string.
     */
    private final int[]  savedVertexColor = new int[4];

    /**
     * Constructs empty instance.
     * @param fileStream opened file output stream where data will be saved
     */
    public BasicVertexConsumer(OutputStream fileStream) {
        outputStream = fileStream;
    }

    /**
     * Copy of {@link VertexConsumer#putBulkData}. Includes control over rendering of ambient occlusion.
     */
    @Override
    public void putBulkData(PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls,
                            float pRed, float pGreen, float pBlue, float alpha, int[] pCombinedLights,
                            int pCombinedOverlay, boolean pMulColor) {
        float[] afloat = new float[]{pColorMuls[0], pColorMuls[1], pColorMuls[2], pColorMuls[3]};
        int[] aint = new int[]{pCombinedLights[0], pCombinedLights[1], pCombinedLights[2], pCombinedLights[3]};
        int[] aint1 = pQuad.getVertices();
        Vec3i vec3i = pQuad.getDirection().getNormal();
        Matrix4f matrix4f = pPoseEntry.pose();
        Vector3f vector3f = pPoseEntry.normal().transform(
                new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ()));
        int i = 8;
        int j = aint1.length / 8;

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for(int k = 0; k < j; ++k) {
                intbuffer.clear();
                intbuffer.put(aint1, k * 8, 8);
                float f = bytebuffer.getFloat(0);
                float f1 = bytebuffer.getFloat(4);
                float f2 = bytebuffer.getFloat(8);
                float f3 = pRed;
                float f4 = pGreen;
                float f5 = pBlue;

                // Brightness was moved from here below
                if (pMulColor) {
                    f3 *= (float)(bytebuffer.get(12) & 255) / 255.0F;
                    f4 *= (float)(bytebuffer.get(13) & 255) / 255.0F;
                    f5 *= (float)(bytebuffer.get(14) & 255) / 255.0F;
                }

                // Ambient occlusion depending on config
                if (Config.DATA.useMinecraftAmbientOcclusion) {
                    f3 *= afloat[k];
                    f4 *= afloat[k];
                    f5 *= afloat[k];
                }

                int l = applyBakedLighting(pCombinedLights[k], bytebuffer);
                float f9 = bytebuffer.getFloat(16);
                float f10 = bytebuffer.getFloat(20);
                Vector4f vector4f = matrix4f.transform(new Vector4f(f, f1, f2, 1.0F));
                applyBakedNormals(vector3f, bytebuffer, pPoseEntry.normal());
                float vertexAlpha = pMulColor ? alpha * (float) (bytebuffer.get(15) & 255) / 255.0F : alpha;
                this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f3, f4, f5, vertexAlpha, f9, f10,
                        pCombinedOverlay, l, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }

    }

    /**
     * Processes vertex.
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
     * Saves vertex coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer vertex(double x, double y, double z) {
        savedVertexCoordinates[0] = x;
        savedVertexCoordinates[1] = y;
        savedVertexCoordinates[2] = z;
        return this;
    }

    /**
     * Saves vertex UVs.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer uv(float u, float v) {
        savedVertexUVs[0] = u;
        savedVertexUVs[1] = v;
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
    public @NotNull VertexConsumer color(int r, int g, int b, int a) {
        savedVertexColor[0] = r;
        savedVertexColor[1] = g;
        savedVertexColor[2] = b;
        savedVertexColor[3] = a;

        return this;
    }

    /**
     * Writes vertex sata, constructed from saved coordinates, UVs and color, to file.
     */
    @Override
    public void endVertex() {
        // ByteBuffer size is 3 double (each is 8 bytes) + 2 floats (each is 4 bytes) + 4 ints (each is 4 bytes)
        byte[] bytes = ByteBuffer.allocate(48)
                .putDouble(savedVertexCoordinates[0]).putDouble(savedVertexCoordinates[1])
                .putDouble(savedVertexCoordinates[2])
                .putFloat(savedVertexUVs[0]).putFloat(savedVertexUVs[1])
                .putInt(savedVertexColor[0]).putInt(savedVertexColor[1])
                .putInt(savedVertexColor[2]).putInt(savedVertexColor[3])
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
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer normal(float x, float y, float z) {
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
