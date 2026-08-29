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
 * D | 0 | return book | 2019-06-06
 * E | 0 | project meeting | 2019-08-06 | 2019-08-07
 * </pre>
 * The first field is the type letter, the second is 1 when the task is done
 * and 0 when it is not, and the rest are the fields that kind of task carries.
 */
public class Storage {
    /**
     * What separates one field from the next on a line of the data file.
     * It is public because a description containing it would split into extra
     * fields and be read back wrongly, so the chatbot checks what the user
     * types against it before storing a task.
     */
    public static final String SEPARATOR = " | ";

    /** The marker written for a task that has been completed. */
    private static final String DONE = "1";

    /** The marker written for a task that has not been completed. */
    private static final String NOT_DONE = "0";

    /**
     * The file this Storage looks after, as the caller wrote it, for example
     * "data/elsa.txt". Kept as text as well as a Path so that messages naming the
     * file read the same on every operating system: Path.toString() would use the
     * separator of whichever one is running.
     */
    private final String name;

    /**
     * Where the task list is kept. The path given is relative, so it is resolved
     * against the folder the program is run from rather than naming one computer's
     * drive, and Path.of turns it into a path the current operating system
     * understands, so the same code works on Windows and on macOS.
     */
    private final Path filePath;

    /**
     * What a load produced: the tasks that could be read, and one message for
     * each line that could not be. Both are needed, because a file with a bad
     * line in it still has good lines worth keeping.
     *
     * <p>This is a record: a class whose only job is to hold a few values
     * together. Java writes the constructor and the tasks() and problems()
     * accessor methods from this one declaration.
     *
     * @param tasks    the tasks read from the file, in the order they appear
     * @param problems one message per line that could not be understood
     */
    public record LoadResult(TaskList tasks, ArrayList<String> problems) {
    }

    /**
     * Creates a store that keeps the tasks in one named file.
     *
     * <p>The file is named by the caller rather than fixed here, so that the one
     * place deciding where the tasks live is the program's starting point, and so
     * that a test could point a Storage at a file of its own. Write the path with
     * forward slashes: every operating system accepts them.
     *
     * @param filePath where to keep the tasks, relative to where the program is run
     */
    public Storage(String filePath) {
        this.name = filePath;
        this.filePath = Path.of(filePath);
    }

    /**
     * Returns the name of the data file, so that messages to the user can say
     * where the tasks are kept without other classes knowing the path itself.
     *
     * @return the path of the data file as text, such as "data/elsa.txt"
     */
    public String getFileName() {
        return name;
    }

    /**
     * Writes the whole task list to the data file, one task per line.
     * The file is replaced each time rather than appended to, so what is on the
     * disk always matches the list in memory exactly, even after a deletion.
     *
     * @param tasks the tasks to save, in the order they appear in the list
     * @throws ElsaException if the file or its folder could not be written
     */
    public void save(TaskList tasks) throws ElsaException {
        // Each task knows how to write itself as a line; see Task.toSaveFormat().
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            lines.add(tasks.get(i).toSaveFormat());
        }

        try {
            // The data folder is not part of the repository, so create it on the
            // first save. createDirectories does nothing if it already exists.
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, lines);
        } catch (IOException e) {
            // Rethrown as an ElsaException so the chatbot reports it the same way
            // as any other problem, instead of a stack trace ending the session.
            // The reason is included because a full disk, a folder that is really
            // a file, and a read-only file all arrive here and need telling apart.
            throw new ElsaException("I could not save your tasks to " + getFileName()
                    + ". The reason given was: " + e);
        }
    }

    /**
     * Reads back the tasks saved by an earlier run.
     * A missing file is not an error: it simply means this is the first run,
     * so an empty result is returned and the file is made by the first save.
     *
     * <p>A line that cannot be understood is left out and described in the
     * result, rather than the whole file being abandoned, so that one damaged
     * line cannot cost the user every other task they had saved.
     *
     * @return the tasks that could be read, and a message per line that could not
     * @throws ElsaException if the file exists but could not be read at all
     */
    public LoadResult load() throws ElsaException {
        ArrayList<Task> tasks = new ArrayList<>();
        ArrayList<String> problems = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return new LoadResult(new TaskList(tasks), problems);
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            // Unreadable, a folder rather than a file, or not text at all:
            // nothing can be salvaged, so this is reported as a failure.
            throw new ElsaException("I could not read your saved tasks from "
                    + getFileName() + ". The reason given was: " + e);
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            // A blank line carries no task, so skip it rather than complain.
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(parseTask(line));
            } catch (ElsaException e) {
                // Line numbers start at 1 for the user, as they do in an editor.
                problems.add("Line " + (i + 1) + ": " + e.getMessage());
            }
        }
        // Wrapped only at the end, so that the reading above works with a plain
        // list and the caller still receives the list in the form it will use it.
        return new LoadResult(new TaskList(tasks), problems);
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
            throw new ElsaException("it has only " + fields.length
                    + " field(s), and every task needs at least 3");
        }

        String description = fields[2];
        if (description.isBlank()) {
            throw new ElsaException("its description is blank");
        }

        Task task;
        // Each kind of task needs a different number of fields, so each branch
        // checks it has them before reading the ones beyond the description.
        switch (fields[0]) {
        case "T" -> task = new Todo(description);
        case "D" -> {
            requireFields(fields, 4);
            task = new Deadline(description, Dates.parse(fields[3]));
        }
        case "E" -> {
            requireFields(fields, 5);
            task = new Event(description, Dates.parse(fields[3]),
                    Dates.parse(fields[4]));
        }
        default -> throw new ElsaException("\"" + fields[0]
                + "\" is not a task type; it should be T, D or E");
        }

        // The second field records whether the task was done when it was saved.
        // Anything other than the two markers means the line cannot be trusted,
        // so it is reported rather than quietly assumed to be not done.
        switch (fields[1]) {
        case DONE -> task.markAsDone();
        case NOT_DONE -> task.markAsNotDone();
        default -> throw new ElsaException("\"" + fields[1]
                + "\" is not a done marker; it should be " + DONE + " or " + NOT_DONE);
        }
        return task;
    }

    /**
     * Checks that a line carries all the fields its kind of task needs.
     *
     * @param fields   the fields the line was split into
     * @param expected how many fields this kind of task needs
     * @throws ElsaException if the line has fewer fields than expected
     */
    private static void requireFields(String[] fields, int expected) throws ElsaException {
        if (fields.length < expected) {
            throw new ElsaException("a " + fields[0] + " task needs " + expected
                    + " fields, but this line has " + fields.length);
        }
    }
}
