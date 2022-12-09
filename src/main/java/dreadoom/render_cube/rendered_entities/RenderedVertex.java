package dreadoom.render_cube.rendered_entities;

/**
 * Contains vertex geometry data.
 */
public class RenderedVertex {
    /**
     * Vertex x coordinate.
     */
    public float x;

    /**
     * Vertex y coordinate.
     */
    public float y;

    /**
     * Vertex z coordinate.
     */
    public float z;

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
        // All coordinates have to be >= 0.0001 and do not have more than 4 numbers after decimal point in order for
        // blender to read them properly
        this.x = (float)(Math.floor(x * 10000.0) / 10000.0);
        if (this.x == 0 && x != 0)
            this.x = (float)(Math.signum(x) * 0.0001);

        this.y = (float)(Math.floor(y * 10000.0) / 10000.0);
        if (this.y == 0 && y != 0)
            this.y = (float)(Math.signum(y) * 0.0001);

        this.z = (float)(Math.floor(z * 10000.0) / 10000.0);
        if (this.z == 0 && z != 0)
            this.z = (float)(Math.signum(z) * 0.0001);

        // This does not apply to UVs tho
        this.u = u;
        this.v = v;
    }
}
