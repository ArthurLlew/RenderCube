package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dreadoom.render_cube.rendered_geometry.RenderedQuad;
import dreadoom.render_cube.rendered_geometry.RenderedVertex;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements basic variables, that stores geometry data, and methods overrides of {@link VertexConsumer}.
 */
public class BasicVertexConsumer implements VertexConsumer {
    /**
     * Holds consumed vertices.
     */
    public List<RenderedVertex> vertices = new ArrayList<>();

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
    private String savedVertexColor;

    /**
     * Constructs empty instance.
     */
    public BasicVertexConsumer(){}

    /**
     * Tries to convert all hold vertices to quads.
     * @return List of rendered quads
     */
    public List<RenderedQuad> convertVerticesToQuads(){
        // Init list
        List<RenderedQuad> quads = new ArrayList<>();

        // Vertices count should be dividable by 4
        if(vertices.size() % 4 == 0){
            // For each pack of 4 vertices
            for(int i = 0; i < vertices.size(); i += 4){
                RenderedQuad quad = new RenderedQuad();

                // Add 4 vertices from pack
                quad.vertices.add(vertices.get(i));
                quad.vertices.add(vertices.get(i + 1));
                quad.vertices.add(vertices.get(i + 2));
                quad.vertices.add(vertices.get(i + 3));

                // Add to list
                quads.add(quad);
            }

            // Clear now unused vertex information
            vertices.clear();
        }

        return quads;
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
        try {
            String buf = Integer.toHexString(new Color(r, g, b, a).getRGB());
            savedVertexColor = "#" + buf.substring(buf.length()-6);
        } catch (Throwable throwable) {
            savedVertexColor = "#ffffff";
        }

        return this;
    }

    /**
     * Adds vertex, constructed from saved coordinates, UVs and color, to vertex list.
     */
    @Override
    public void endVertex(){
        vertices.add(new RenderedVertex(
                savedVertexCoordinates[0],
                savedVertexCoordinates[1],
                savedVertexCoordinates[2],
                savedVertexUVs[0],
                savedVertexUVs[1],
                savedVertexColor));
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
