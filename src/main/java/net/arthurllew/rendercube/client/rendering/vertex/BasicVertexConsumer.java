package net.arthurllew.rendercube.client.rendering.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;

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
    public BasicVertexConsumer(OutputStream fileStream){
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
     * Saves vertex coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z){
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
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a){
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
    public @NotNull VertexConsumer setUv(float u, float v){
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
    public @NotNull VertexConsumer setOverlay(int packedOverlay){
        return this;
    }

    /**
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setUv1(int var1, int var2){
        return this;
    }

    /**
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setUv2(int var1, int var2){
        return this;
    }

    /**
     * Does nothing.
     * @return self
     */
    @Override
    public @NotNull VertexConsumer setNormal(float x, float y, float z){
        return this;
    }
}
