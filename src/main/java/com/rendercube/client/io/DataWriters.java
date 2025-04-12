package com.rendercube.client.io;

import com.rendercube.RenderCube;

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
        blockWriter= new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedBlocks" + FileExtension), bufferSize);
        liquidWriter = new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedLiquids" + FileExtension), bufferSize);
        blockEntityWriter = new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedBlockEntities" + FileExtension), bufferSize);
        entityWriter = new BufferedOutputStream(
                new FileOutputStream(dirName + "\\" + "renderedEntities" + FileExtension), bufferSize);
    }

    /**
     * Closes file writers.
     * @throws IOException when file exceptions are encountered.
     */
    @Override
    public void close() throws IOException {
        blockWriter.close();
        liquidWriter.close();
        blockEntityWriter.close();
        entityWriter.close();
    }
}
