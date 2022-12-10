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
     * @param x vertex x coordinate
     * @param y vertex y coordinate
     * @param z vertex z coordinate
     * @param u vertex u coordinate
     * @param v vertex v coordinate
     */
    public RenderedVertex(double x, double y, double z, float u, float v){
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
    }
}
