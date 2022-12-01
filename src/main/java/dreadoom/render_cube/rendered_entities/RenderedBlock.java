package dreadoom.render_cube.rendered_entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains block geometry data and block position.
 */
public class RenderedBlock {
    /**
     * Block x coordinate
     */
    public float x;

    /**
     * Block y coordinate
     */
    public float y;

    /**
     * Block z coordinate
     */
    public float z;

    /**
     * List of block render-able quads
     */
    public List<RenderedQuad> quads = new ArrayList<>();

    /**
     * Constructs instance from block coordinates. The quads list is initialized to empty.
     * @param in_x block x coordinate
     * @param in_y block y coordinate
     * @param in_z block z coordinate
     */
    public RenderedBlock(float in_x, float in_y, float in_z){
        x = in_x;
        y = in_y;
        z = in_z;
    }
}
