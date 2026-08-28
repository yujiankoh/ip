import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * Keeps the task list on the hard disk so that it outlives a single run of the
 * chatbot. Only saving is implemented for now; loading the file back at startup
 * is left for a later increment.
 */
public class Storage {
    /**
     * Where the task list is kept, relative to the folder the program is run from
     * (the project root). Path.of joins the parts with the separator the current
     * operating system uses, so the same code works on Windows and on macOS.
     */
    private static final Path FILE_PATH = Path.of("data", "elsa.txt");

    /**
     * Writes the whole task list to the data file, one task per line.
     * The file is replaced each time rather than appended to, so what is on the
     * disk always matches the list in memory exactly, even after a deletion.
     *
     * @param tasks the tasks to save, in the order they appear in the list
     * @throws ElsaException if the file or its folder could not be written
     */
    public static void save(ArrayList<Task> tasks) throws ElsaException {
        // Each task knows how to write itself as a line; see Task.toSaveFormat().
        ArrayList<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toSaveFormat());
        }

        try {
            // The data folder is not part of the repository, so create it on the
            // first save. createDirectories does nothing if it already exists.
            Files.createDirectories(FILE_PATH.getParent());
            Files.write(FILE_PATH, lines);
        } catch (IOException e) {
            // Rethrown as an ElsaException so the chatbot reports it the same way
            // as any other problem, instead of the stack trace ending the session.
            throw new ElsaException("I could not save your tasks to " + FILE_PATH + ".");
        }
    }
}
