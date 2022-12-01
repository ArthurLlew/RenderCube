package dreadoom.render_cube.rendered_entities;

/**
 * Contains vertex geometry data.
 */
public class RenderedVertex {
    /**
     * Vertex x coordinate.
     */
    public double x;

    /**
     * Vertex y coordinate.
     */
    public double y;

    /**
     * Vertex z coordinate.
     */
    public double z;

    /**
     * Vertex u coordinate.
     */
    public float u;

    /**
     * Vertex v coordinate.
     */
    public float v;

    /**
     * Constructs instance from given coordinates.
     * @param in_x vertex x coordinate
     * @param in_y vertex y coordinate
     * @param in_z vertex z coordinate
     * @param in_u vertex u coordinate
     * @param in_v vertex v coordinate
     */
    public RenderedVertex(double in_x, double in_y, double in_z, float in_u, float in_v){
        x = in_x;
        y = in_y;
        z = in_z;
        u = in_u;
        v = in_v;
    }
}
