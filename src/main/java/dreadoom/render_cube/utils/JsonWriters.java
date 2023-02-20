package dreadoom.render_cube.utils;

import dreadoom.render_cube.RenderCube;

import java.io.IOException;

/**
 * Autocloseable collection of json writers.
 */
public class JsonWriters implements AutoCloseable {
    /**
     * Holds instance of rendered blocks writer.
     */
    public JsonSequenceWriter blockWriter;

    /**
     * Holds instance of rendered liquids writer.
     */
    public JsonSequenceWriter liquidWriter;

    /**
     * Holds instance of rendered block entities writer.
     */
    public JsonSequenceWriter blockEntityWriter;

    /**
     * Holds instance of rendered entities writer.
     */
    public JsonSequenceWriter entityWriter;

    /**
     * Writers init.
     * @throws IOException when file exceptions are encountered
     */
    public JsonWriters() throws IOException {
        blockWriter = new JsonSequenceWriter(RenderCube.MODID + "\\" + "renderedBlocks.json");
        liquidWriter = new JsonSequenceWriter(RenderCube.MODID + "\\" + "renderedLiquids.json");
        blockEntityWriter = new JsonSequenceWriter(RenderCube.MODID + "\\" + "renderedBlockEntities.json");
        entityWriter = new JsonSequenceWriter(RenderCube.MODID + "\\" + "renderedEntities.json");
    }

    /**
     * Closes file writers.
     * @throws IOException when file exceptions are encountered
     */
    @Override
    public void close() throws IOException {
        blockWriter.close();
        liquidWriter.close();
        blockEntityWriter.close();
        entityWriter.close();
    }
}
