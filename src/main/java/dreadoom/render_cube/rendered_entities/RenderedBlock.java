package dreadoom.render_cube.rendered_entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains block geometry data and block position.
 */
public class RenderedBlock {
    /**
     * Block type
     */
    public RenderedBlockType type;

    /**
     * Block x coordinate.
     */
    public float x;

    /**
     * Block y coordinate.
     */
    public float y;

    /**
     * Block z coordinate.
     */
    public float z;

    /**
     * List of block render-able quads.
     */
    public List<RenderedQuad> quads = new ArrayList<>();

    /**
     * Constructs instance from block coordinates. The quads list is initialized to empty.
     * @param type block type
     * @param x block x coordinate
     * @param y block y coordinate
     * @param z block z coordinate
     */
    public RenderedBlock(RenderedBlockType type, float x, float y, float z){
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
