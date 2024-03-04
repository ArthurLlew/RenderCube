package com.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Implements basic variables, that stores geometry data, and methods overrides of {@link VertexConsumer}.
 */
public class BasicVertexConsumer implements VertexConsumer{
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
    public BasicVertexConsumer(OutputStream fileStream){
        outputStream = fileStream;
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
    public void vertex(float x,
                       float y,
                       float z,
                       float r,
                       float g,
                       float b,
                       float a,
                       float u,
                       float v,
                       int overlayCords,
                       int uv2,
                       float normal_x,
                       float normal_y,
                       float normal_z){
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
    public @NotNull VertexConsumer vertex(double x, double y, double z){
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
    public @NotNull VertexConsumer uv(float u, float v){
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
    public @NotNull VertexConsumer color(int r, int g, int b, int a){
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
    public void endVertex(){
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
    public @NotNull VertexConsumer overlayCoords(int u, int v){
        return this;
    }

    /**
     * Does nothing.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer uv2(int u, int v){
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
    public @NotNull VertexConsumer normal(float x, float y, float z){
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
    public void defaultColor(int r, int g, int b, int a){
    }

    /**
     * Does nothing.
     */
    @Override
    public void unsetDefaultColor(){
    }
}
