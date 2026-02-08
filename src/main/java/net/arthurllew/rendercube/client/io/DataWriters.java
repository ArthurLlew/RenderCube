package net.arthurllew.rendercube.client.io;

import net.arthurllew.rendercube.RenderCube;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * Autocloseable collection of file writers.
 */
public class DataWriters implements AutoCloseable {
    /**
     * Holds instance of rendered blocks writer.
     */
    final public OutputStream blockWriter;
    /**
     * Holds instance of rendered blocks writer.
     */
    final public OutputStream vegetationWriter;

    /**
     * Holds instance of rendered liquids writer.
     */
    final public OutputStream liquidWriter;

    /**
     * Holds instance of rendered block entities writer.
     */
    final public OutputStream blockEntityWriter;

    /**
     * Holds instance of rendered entities writer.
     */
    final public OutputStream entityWriter;

    /**
     * Writers init.
     * @throws IOException when file exceptions are encountered.
     */
    public DataWriters() throws IOException {
        // Date time string
        String dateTimeStr = LocalDateTime.now().toString()
                .replace("T", "_").replace(":", "-");
        dateTimeStr =  dateTimeStr.substring(0, dateTimeStr.lastIndexOf("."));

        // Create appropriate directory
        String dirName = RenderCube.MODID + "\\" + dateTimeStr;
        Files.createDirectories(Paths.get(dirName));

        // Create file writers
        int bufferSize = 8064;	// Buffer size = 48 (size of one vertex) * 4 (4 in a quad) * 42 (arbitrary number)
        String FileExtension = ".rcube";
        this.blockWriter= new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedBlocks" + FileExtension), bufferSize);
        this.vegetationWriter= new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedVegetation" + FileExtension), bufferSize);
        this.liquidWriter = new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedLiquids" + FileExtension), bufferSize);
        this.blockEntityWriter = new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedBlockEntities" + FileExtension), bufferSize);
        this.entityWriter = new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedEntities" + FileExtension), bufferSize);
    }

    /**
     * Closes file writers.
     * @throws IOException when file exceptions are encountered.
     */
    @Override
    public void close() throws IOException {
        this.blockWriter.close();
        this.vegetationWriter.close();
        this.liquidWriter.close();
        this.blockEntityWriter.close();
        this.entityWriter.close();
    }
}
