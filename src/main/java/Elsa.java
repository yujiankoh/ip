import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores todos, deadlines and events, lists them back on request,
 * marks them as done or not done, deletes them, lists those falling on a given
 * date, reports what it cannot understand,
 * and exits when the user types "bye".
 * The task list is saved to the hard disk every time it changes and is read back
 * at startup; see {@link Storage}.
 *
 * <p>What the user sees and types is handled by {@link Ui}, so this class is left
 * with working out what each command means and carrying it out.
 */
public class Elsa {
    /** Separates a deadline's description from the date it is due. */
    private static final String BY_SEPARATOR = "/by";

    /** Separates an event's description from its start date. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start date from its end date. */
    private static final String TO_SEPARATOR = "/to";

    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        // An ArrayList grows as tasks are added, so there is no fixed capacity to track
        // separately: size() is always exactly how many tasks there are.
        // Tasks saved by an earlier run are read back here, so the list picks up where
        // the user left off. On the very first run there is no file yet, and load()
        // returns an empty list rather than treating that as a problem.
        ArrayList<Task> tasks;
        try {
            Storage.LoadResult loaded = Storage.load();
            tasks = loaded.tasks();
            if (!loaded.problems().isEmpty()) {
                // The tasks that did load are kept, so the user is told what was
                // lost rather than the whole file being thrown away.
                ui.showSkippedLines(loaded.problems());
            }
        } catch (ElsaException e) {
            // A file that cannot be understood is reported once, and the session goes
            // on with an empty list rather than refusing to start.
            ui.showError(e.getMessage());
            tasks = new ArrayList<>();
        }

        // The flag ends the loop from inside the switch, where a plain break would only
        // leave the switch. hasNextCommand() guards against input ending without a "bye".
        boolean isRunning = true;
        while (isRunning && ui.hasNextCommand()) {
            String line = ui.readCommand();

            // Every line is one keyword plus whatever follows it. The split is on a run
            // of whitespace so that a tab, or several spaces, separates them just as one
            // space does. Splitting here means
            // "todo" with nothing after it is recognised as a todo missing its description,
            // rather than being mistaken for an unknown command.
            String[] words = line.split("\\s+", 2);
            Command command = Command.fromKeyword(words[0]);
            String arguments = (words.length > 1) ? words[1].trim() : "";

            try {
                switch (command) {
                case BYE -> {
                    ui.showFarewell();
                    isRunning = false;
                }
                case LIST -> ui.showTasks(tasks);
                case ON -> {
                    if (arguments.isEmpty()) {
                        throw new ElsaException("Which date? Use: " + command.getUsage()
                                + ", for example: on 2019-10-15.");
                    }
                    ui.showTasksOn(tasks, requireDate(arguments, command));
                }
                case MARK -> {
                    int index = parseTaskIndex(arguments, tasks.size(), command);
                    tasks.get(index).markAsDone();
                    Storage.save(tasks);
                    ui.showMarked(tasks.get(index));
                }
                case UNMARK -> {
                    int index = parseTaskIndex(arguments, tasks.size(), command);
                    tasks.get(index).markAsNotDone();
                    Storage.save(tasks);
                    ui.showUnmarked(tasks.get(index));
                }
                case DELETE -> {
                    int index = parseTaskIndex(arguments, tasks.size(), command);
                    // remove() returns the task it took out, so it can be shown to the user.
                    Task removed = tasks.remove(index);
                    Storage.save(tasks);
                    ui.showRemoved(removed, tasks.size());
                }
                case TODO -> {
                    requireDescription(arguments, command);
                    requireNoSeparator(arguments, "description of a todo", command);
                    addTask(ui, tasks, new Todo(arguments));
                }
                case DEADLINE -> {
                    requireDescription(arguments, command);
                    // Limit of 2 keeps any later "/by" as part of the due date itself.
                    String[] parts = requireSeparator(arguments, BY_SEPARATOR, command);
                    String description = requireNonEmpty(parts[0],
                            "description of a deadline", command);
                    LocalDate by = requireDate(requireNonEmpty(parts[1],
                            "due date after " + BY_SEPARATOR, command), command);
                    addTask(ui, tasks, new Deadline(description, by));
                }
                case EVENT -> {
                    requireDescription(arguments, command);
                    // Split off the description first, then split what remains into the two dates.
                    String[] parts = requireSeparator(arguments, FROM_SEPARATOR, command);
                    String description = requireNonEmpty(parts[0],
                            "description of an event", command);
                    String[] times = requireSeparator(parts[1], TO_SEPARATOR, command);
                    LocalDate from = requireDate(requireNonEmpty(times[0],
                            "start date after " + FROM_SEPARATOR, command), command);
                    LocalDate to = requireDate(requireNonEmpty(times[1],
                            "end date after " + TO_SEPARATOR, command), command);
                    addTask(ui, tasks, new Event(description, from, to));
                }
                case NOTHING -> throw new ElsaException("You did not type anything. Try \""
                        + Command.TODO.getUsage() + "\", or \"list\" to see what you have.");
                case UNKNOWN -> throw new ElsaException(
                        "I'm sorry, but I don't know what that means :-(");
                }
            } catch (ElsaException e) {
                // One place to report anything the chatbot could not carry out.
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Adds a task to the list, saves the updated list to the hard disk, and
     * confirms the addition to the user. The three commands that add a task all
     * do these same three things, so they share this method.
     *
     * @param ui    the user interface that confirms the addition
     * @param tasks the task list to add to
     * @param task  the task the user asked to add
     * @throws ElsaException if the updated list could not be saved
     */
    private static void addTask(Ui ui, ArrayList<Task> tasks, Task task)
            throws ElsaException {
        tasks.add(task);
        // Saved before the confirmation is shown, so the chatbot never claims to
        // have stored a task that did not reach the disk.
        Storage.save(tasks);
        ui.showAdded(task, tasks.size());
    }

    /**
     * Checks that a command that adds a task was given something to add.
     *
     * @param arguments everything the user typed after the command keyword
     * @param command   the command being run, which supplies its own name and usage
     * @throws ElsaException if nothing was typed after the keyword
     */
    private static void requireDescription(String arguments, Command command)
            throws ElsaException {
        if (arguments.isEmpty()) {
            String keyword = command.getKeyword();
            // "an event" but "a todo": pick the article that reads correctly.
            String article = ("aeiou".indexOf(keyword.charAt(0)) >= 0) ? "an" : "a";
            throw new ElsaException("The description of " + article + " " + keyword
                    + " cannot be empty. Use: " + command.getUsage());
        }
    }

    /**
     * Splits text on a separator the command requires, reporting its absence to the user.
     *
     * @param text      the text to split
     * @param separator the separator the command cannot do without, such as "/by"
     * @param command   the command being run, which supplies the usage to show
     * @return the two pieces on either side of the first occurrence of the separator
     * @throws ElsaException if the separator does not appear in the text
     */
    private static String[] requireSeparator(String text, String separator, Command command)
            throws ElsaException {
        // Limit of 2 keeps any later occurrence as part of the second piece.
        String[] parts = text.split(separator, 2);
        if (parts.length < 2) {
            throw new ElsaException("I could not find \"" + separator + "\" in that. Use: "
                    + command.getUsage());
        }
        return parts;
    }

    /**
     * Reads a piece of a command as a date, saying how to write one if it is not.
     * The date itself is understood by {@link Dates}; this method only adds the
     * usage of the command being run, so the user can see the whole line again.
     *
     * @param value   the text the user gave as a date
     * @param command the command being run, which supplies the usage to show
     * @return the date that text describes
     * @throws ElsaException if the text is not a date
     */
    private static LocalDate requireDate(String value, Command command)
            throws ElsaException {
        try {
            return Dates.parse(value);
        } catch (ElsaException e) {
            throw new ElsaException(e.getMessage() + ". Use: " + command.getUsage());
        }
    }

    /**
     * Checks that a piece of a command does not contain the text that separates
     * one field from the next in the data file. A description holding that text
     * would be split into extra fields when the file is read back, so the task
     * would return changed, or not at all. Refusing it now is clearer to the
     * user than losing part of their task later.
     *
     * @param value   the piece to check
     * @param what    what the piece is, named for the error message
     * @param command the command being run, which supplies the usage to show
     * @throws ElsaException if the piece contains the separator
     */
    private static void requireNoSeparator(String value, String what, Command command)
            throws ElsaException {
        if (value.contains(Storage.SEPARATOR)) {
            throw new ElsaException("The " + what + " cannot contain \""
                    + Storage.SEPARATOR.trim() + "\" with a space on each side, because"
                    + " that is how " + Storage.getFileName() + " separates the parts of a"
                    + " task. Use: " + command.getUsage());
        }
    }

    /**
     * Checks that a piece of a command was actually filled in.
     *
     * @param value   the piece to check, before trimming
     * @param what    what the piece is, named for the error message
     * @param command the command being run, which supplies the usage to show
     * @return the value with surrounding spaces removed
     * @throws ElsaException if the piece is empty once trimmed
     */
    private static String requireNonEmpty(String value, String what, Command command)
            throws ElsaException {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ElsaException("The " + what + " cannot be empty. Use: "
                    + command.getUsage());
        }
        requireNoSeparator(trimmed, what, command);
        return trimmed;
    }

    /**
     * Converts the task number typed by the user into a list index,
     * checking that it is a whole number and that a task with that number exists.
     *
     * @param arguments everything the user typed after the command keyword
     * @param taskCount how many tasks are stored, so the number can be range checked
     * @param command   the command being run, used to word the error messages
     * @return the corresponding 0-based index into the task list
     * @throws ElsaException if no number was given, it is not a whole number,
     *                       or no task has that number
     */
    private static int parseTaskIndex(String arguments, int taskCount, Command command)
            throws ElsaException {
        String keyword = command.getKeyword();
        if (arguments.isEmpty()) {
            throw new ElsaException("Which task? Use: " + keyword
                    + " <task number>, for example: " + keyword + " 2.");
        }

        int number;
        try {
            number = Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            // Rethrown as an ElsaException so it reaches the user instead of ending the session.
            throw new ElsaException("\"" + arguments + "\" is not a task number. Use a whole"
                    + " number, for example: " + keyword + " 2.");
        }

        if (taskCount == 0) {
            throw new ElsaException("There are no tasks yet, so there is nothing to "
                    + keyword + ". Add one with \"" + Command.TODO.getUsage() + "\" first.");
        }
        if (number < 1 || number > taskCount) {
            String plural = (taskCount == 1) ? "task" : "tasks";
            throw new ElsaException("There is no task " + number + ". You have "
                    + taskCount + " " + plural + ", so use a number from 1 to " + taskCount + ".");
        }

        // The user counts from 1, so subtract 1 to get the list index.
        return number - 1;
    }
}
