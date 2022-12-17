package dreadoom.render_cube.rendered_entities;

/**
 * Contains cube position and block, entity and liquid geometry data in a cube.
 */
public class RenderedCube {
    /**
     * Cube X coordinate.
     */
    public float x;

    /**
     * Cube Y coordinate.
     */
    public float y;

    /**
     * Cube Z coordinate.
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
