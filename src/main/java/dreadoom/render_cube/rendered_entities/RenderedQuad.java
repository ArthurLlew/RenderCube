package dreadoom.render_cube.rendered_entities;

import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains quad geometry data.
 */
public class RenderedQuad {
    /**
     * List of quad vertices.
     */
    public List<RenderedVertex> vertices = new ArrayList<>();

    /**
     * Quad color as hex string.
     */
    public String quadColor;

    public RenderedQuad(){}

    /**
     * Constructs instance from provided {@link BakedQuad}.
     */
    @Deprecated
    public RenderedQuad(BakedQuad quad){
        // Geometric encrypted information
        int[] encryptedVertices = quad.getVertices();

        // Array containing x, y, z, u, v in succession
        float[] cords = new float[5];

        // For all array elements
        for (int j = 0; j < encryptedVertices.length; j++) {
            /*
            Each pack of 8 values in array contain 3 vertex point coordinates. They are in the beginning of
            the pack. U and V coordinates are in 4-th and 5-th positions respectively
            */

            // get index in 8-pack values
            int n = j % 8;

            // Action, depending on n
            switch (n) {
                // X coordinate
                case 0 -> cords[0] = Float.intBitsToFloat(encryptedVertices[j]);
                // Y coordinate
                case 1 -> cords[1] = Float.intBitsToFloat(encryptedVertices[j]);
                // Z coordinate
                case 2 -> cords[2] = Float.intBitsToFloat(encryptedVertices[j]);
                // U coordinate
                case 4 -> cords[3] = Float.intBitsToFloat(encryptedVertices[j]);
                // V coordinate
                case 5 -> {
                    cords[4] = Float.intBitsToFloat(encryptedVertices[j]);

                    // Add vertex to quad
                    vertices.add(new RenderedVertex(cords[0], cords[1], cords[2], cords[3] ,cords[4]));
                }
            }
        }
    }
}
