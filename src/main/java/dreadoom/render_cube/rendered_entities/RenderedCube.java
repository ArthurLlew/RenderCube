package dreadoom.render_cube.rendered_entities;

/**
 * Contains block, entity and liquid geometry data in a cube and cube position.
 */
public class RenderedCube {
    /**
     * Cube x coordinate.
     */
    public float x;

    /**
     * Cube y coordinate.
     */
    public float y;

    /**
     * Cube z coordinate.
     */
    public float z;

    /**
     * Contains block geometry.
     */
    public RenderedModel renderedBlock;

    /**
     * Contains entity geometry.
     */
    public RenderedModel renderedEntity;

    /**
     * Contains liquid geometry.
     */
    public RenderedModel renderedLiquid;

    /**
     * Constructs instance from block coordinates and rendered block, entity and liquid.
     * @param x block x coordinate
     * @param y block y coordinate
     * @param z block z coordinate
     */
    public RenderedCube(
            float x,
            float y,
            float z,
            RenderedModel renderedBlock,
            RenderedModel renderedEntity,
            RenderedModel renderedLiquid){
        this.x = x;
        this.y = y;
        this.z = z;
        this.renderedBlock = renderedBlock;
        this.renderedEntity = renderedEntity;
        this.renderedLiquid = renderedLiquid;
    }
}
