package net.arthurllew.rendercube.client.io;

import net.arthurllew.rendercube.RenderCube;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Autocloseable collection of file writers.
 */
public class DataWriters implements AutoCloseable {
    /**
     * Writers map containing pairs of writer name and {@link OutputStream}.
     */
    private final Map<String, OutputStream> writers = new HashMap<>();

    /**
     * Subdirectory named after datetime to store files.
     */
    private final String directory;

    /**
     * Basic init.
     */
    public DataWriters() throws IOException {
        // Date time string
        String dateTimeStr = LocalDateTime.now().toString()
                .replace("T", "_").replace(":", "-");
        dateTimeStr =  dateTimeStr.substring(0, dateTimeStr.lastIndexOf("."));

        // Create appropriate directory
        this.directory = RenderCube.MODID + "\\" + dateTimeStr;
        Files.createDirectories(Paths.get(this.directory));
    }

    /**
     * @param fileName name of file writer (is also a filename).
     * @return already existing or newly created file writer.
     * @throws IOException when file exceptions are encountered.
     */
    public OutputStream get(String fileName) throws IOException {
        // Get already existing file writer
        if (writers.containsKey(fileName)) {
            return writers.get(fileName);
        }
        // Create a new one
        else {
            OutputStream writer = new BufferedOutputStream(
                    new FileOutputStream(this.directory + "\\" + fileName + ".rcube"),
                                         // Buffer size = 48 (size of one vertex) * 4 (4 in a quad) *
                                         // * 42 (arbitrary number)
                                         8064);

            writers.put(fileName, writer);

            return writer;
        }
    }

    /**
     * Closes file writers.
     * @throws IOException when file exceptions are encountered.
     */
    @Override
    public void close() throws IOException {
        for (OutputStream writer : writers.values()) {
            writer.close();
        }
    }
}
