package net.arthurllew.rendercube.client.io;

import net.arthurllew.rendercube.RenderCube;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    private final Path directory;

    /**
     * Modification function, applied to file name before opening stream.
     */
    private final Function<String, String> fileNameModifier;

    /**
     * Constructor.
     *
     * @param subdirectory directory inside mod directory where writers will put data
     * @param fileNameModifier function, applied to file name before opening stream
     */
    public DataWriters(String subdirectory,
                       Function<String, String> fileNameModifier) throws IOException {
        // Init directory and make sure it exists
        this.directory = Paths.get(RenderCube.MODID, subdirectory);
        Files.createDirectories(this.directory);

        this.fileNameModifier = fileNameModifier;
    }

    /**
     * @param fileName name of file writer (is also a name of file).
     * @return already existing or newly created file writer.
     * @throws IOException when file exceptions are encountered.
     */
    public OutputStream get(String fileName) throws IOException {
        // Modify file name
        fileName = fileNameModifier.apply(fileName);

        // Get already existing file writer
        if (writers.containsKey(fileName)) {
            return writers.get(fileName);
        }
        // Create a new one
        else {
            OutputStream writer = new BufferedOutputStream(
                    new FileOutputStream(Paths.get(this.directory.toString(), fileName + ".rcube").toString()),
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
