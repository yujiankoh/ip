import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps the task list on the hard disk so that it outlives a single run of the
 * chatbot: {@link #save} writes the list after every change, and {@link #load}
 * reads it back when the chatbot starts.
 *
 * <p>Each line of the file holds one task, with its fields separated by " | ":
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | June 6th
 * E | 0 | project meeting | Aug 6th 2pm | 4pm
 * </pre>
 * The first field is the type letter, the second is 1 when the task is done
 * and 0 when it is not, and the rest are the fields that kind of task carries.
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

    /**
     * Reads back the tasks saved by an earlier run.
     * A missing file is not an error: it simply means this is the first run,
     * so an empty list is returned and the file is created by the first save.
     *
     * @return the saved tasks, in the order they were written
     * @throws ElsaException if the file exists but could not be read or understood
     */
    public static ArrayList<Task> load() throws ElsaException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(FILE_PATH)) {
            return tasks;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(FILE_PATH);
        } catch (IOException e) {
            throw new ElsaException("I could not read your saved tasks from "
                    + FILE_PATH + ".");
        }

        for (String line : lines) {
            // A blank line carries no task, so skip it rather than reject the file.
            if (line.isBlank()) {
                continue;
            }
            tasks.add(parseTask(line));
        }
        return tasks;
    }

    /**
     * Turns one line of the data file back into a task.
     * This is the reverse of {@link Task#toSaveFormat()}: the type letter chooses
     * which kind of task to build, and the fields after it fill that task in.
     *
     * @param line one line of the data file
     * @return the task the line describes
     * @throws ElsaException if the line does not follow the format above
     */
    private static Task parseTask(String line) throws ElsaException {
        // The separator is a literal " | ". split() takes a regular expression,
        // in which "|" means "or", so it is escaped here to mean an ordinary bar.
        String[] fields = line.split(" \\| ");
        // Every task has at least a type letter, a done marker and a description.
        if (fields.length < 3) {
            throw new ElsaException(corruptedMessage(line));
        }

        String description = fields[2];
        Task task;
        // Each kind of task needs a different number of fields, so each branch
        // checks it has them before reading the ones beyond the description.
        switch (fields[0]) {
        case "T" -> task = new Todo(description);
        case "D" -> {
            requireFields(fields, 4, line);
            task = new Deadline(description, fields[3]);
        }
        case "E" -> {
            requireFields(fields, 5, line);
            task = new Event(description, fields[3], fields[4]);
        }
        default -> throw new ElsaException("This line of " + FILE_PATH
                + " starts with an unknown task type: " + line);
        }

        // The second field records whether the task was done when it was saved.
        if (fields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Checks that a line carries all the fields its kind of task needs.
     *
     * @param fields   the fields the line was split into
     * @param expected how many fields this kind of task needs
     * @param line     the whole line, quoted back in the error message
     * @throws ElsaException if the line has fewer fields than expected
     */
    private static void requireFields(String[] fields, int expected, String line)
            throws ElsaException {
        if (fields.length < expected) {
            throw new ElsaException(corruptedMessage(line));
        }
    }

    /**
     * Builds the message shown when a line of the data file cannot be understood.
     *
     * @param line the line at fault, quoted back so the user can go and fix it
     * @return the explanation to show the user
     */
    private static String corruptedMessage(String line) {
        return "This line of " + FILE_PATH + " is missing fields: " + line;
    }
}
