package com.shermobeaver.render_cube.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * This class creates autocloseable json
 */
public class JsonSequenceWriter implements AutoCloseable {
    /**
     * Stores file writer
     */
    private final PrintWriter writer;

    /**
     * Stores json sequence writer
     */
    public final SequenceWriter seqWriter;

    /**
     * Init (opens file)
     * @param filename filename of file to be created
     * @throws IOException when file exceptions are encountered
     */
    public JsonSequenceWriter(String filename) throws IOException {
        // Validate mod directory
        RenderCubeUtils.checkAndCreateModDir();

        // Init usual writer
        writer = new PrintWriter(filename, StandardCharsets.UTF_8);

        // Json object mapper
        ObjectMapper mapper = new ObjectMapper();

        // Try to init sequence writer
        seqWriter = mapper.writer().writeValuesAsArray(writer);
    }

    /**
     * Closes file
     * @throws IOException when file exceptions are encountered
     */
    @Override
    public void close() throws IOException {
        // Close sequence writer
        seqWriter.close();

        // Close file
        writer.close();
    }
}
