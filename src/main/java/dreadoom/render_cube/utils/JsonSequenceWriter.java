package dreadoom.render_cube.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * This class creates autocloseable json sequence writer.
 */
public class JsonSequenceWriter implements AutoCloseable {
    /**
     * Stores file writer.
     */
    private final PrintWriter writer;

    /**
     * Stores json sequence writer.
     */
    public final SequenceWriter seqWriter;

    /**
     * Opens file and creates instance of {@link SequenceWriter}.
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
     * Closes file writers.
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
