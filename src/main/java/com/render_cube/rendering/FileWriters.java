package com.render_cube.rendering;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.render_cube.RenderCube.MODID;

/**
 * Autocloseable collection of file writers.
 */
public class FileWriters implements AutoCloseable {
    /**
     * Holds instance of rendered blocks writer.
     */
    public OutputStream blockWriter;

    /**
     * Holds instance of rendered liquids writer.
     */
    public OutputStream liquidWriter;

    /**
     * Holds instance of rendered block entities writer.
     */
    public OutputStream blockEntityWriter;

    /**
     * Holds instance of rendered entities writer.
     */
    public OutputStream entityWriter;

    /**
     * Writers init.
     * @throws IOException when file exceptions are encountered
     */
    public FileWriters() throws IOException {
        int bufferSize = 8064;	// Buffer size: 48 (size of one vertex) * 4 (4 in a quad) * 42
        String FileExtension = ".rcube";

        blockWriter= new BufferedOutputStream(
                new FileOutputStream(MODID + "\\" + "renderedBlocks" + FileExtension), bufferSize);
        liquidWriter = new BufferedOutputStream(
                new FileOutputStream(MODID + "\\" + "renderedLiquids" + FileExtension), bufferSize);
        blockEntityWriter = new BufferedOutputStream(
                new FileOutputStream(MODID + "\\" + "renderedBlockEntities" + FileExtension), bufferSize);
        entityWriter = new BufferedOutputStream(
                new FileOutputStream(MODID + "\\" + "renderedEntities" + FileExtension), bufferSize);
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
