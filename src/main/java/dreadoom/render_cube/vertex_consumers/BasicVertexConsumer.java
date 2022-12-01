package dreadoom.render_cube.vertex_consumers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dreadoom.render_cube.rendered_entities.RenderedQuad;
import dreadoom.render_cube.rendered_entities.RenderedVertex;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements basic variables and methods of {@link VertexConsumer} overrides.
 */
public abstract class BasicVertexConsumer implements VertexConsumer {
    /**
     * Holds consumed quads.
     */
    public List<RenderedQuad> quads = new ArrayList<>();

    /**
     * Holds consumed vertices.
     */
    public List<RenderedVertex> vertices = new ArrayList<>();

    /**
     * Saved vertex coordinates
     */
    private final double[] savedVertexCoordinates = new double[3];

    /**
     * Saved UV coordinates
     */
    private final float[] savedUvCoordinates = new float[2];

    /**
     * Does nothing.
     * @param pose instance of {@link PoseStack.Pose}
     * @param quad instance of {@link BakedQuad}
     * @param p_85998_ idk
     * @param p_85999_ idk
     * @param p_86000_ idk
     * @param p_86001_ idk
     * @param p_86002_ idk
     * @param p_86003_ idk
     * @param p_86004_ idk
     */
    @Override
    public void putBulkData(@NotNull PoseStack.Pose pose,
                            @NotNull BakedQuad quad,
                            float @NotNull [] p_85998_,
                            float p_85999_,
                            float p_86000_,
                            float p_86001_,
                            int @NotNull [] p_86002_,
                            int p_86003_,
                            boolean p_86004_) {
        quads.add(new RenderedQuad(quad));
    }

    /**
     * Tries to convert all hold vertices to quads.
     */
    public void convertVerticesToQuads(){
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
        }
    }

    /**
     * Does nothing.
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
        this.endVertex();
    }

    /**
     * Does nothing.
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
     * Does nothing.
     * @param u U coordinate
     * @param v V coordinate
     * @return self
     */
    @Override
    public @NotNull VertexConsumer uv(float u, float v){
        savedUvCoordinates[0] = u;
        savedUvCoordinates[1] = v;
        return this;
    }

    /**
     * Does nothing.
     */
    @Override
    public void endVertex(){
        vertices.add(new RenderedVertex(
                savedVertexCoordinates[0],
                savedVertexCoordinates[1],
                savedVertexCoordinates[2],
                savedUvCoordinates[0],
                savedUvCoordinates[1]));
    }

    /**
     * Does nothing.
     * @param r Red channel
     * @param g Green channel
     * @param b Blue channel
     * @param a Alpha channel
     * @return self
     */
    @Override
    public @NotNull VertexConsumer color(int r, int g, int b, int a){
        return this;
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
