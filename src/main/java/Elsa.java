import java.util.ArrayList;
import java.util.Scanner;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores todos, deadlines and events, lists them back on request,
 * marks them as done or not done, deletes them, reports what it cannot understand,
 * and exits when the user types "bye".
 * The task list is saved to the hard disk every time it changes and is read back
 * at startup; see {@link Storage}.
 */
public class Elsa {
    /**
     * Horizontal line that separates one message block from the next.
     * Drawn as a row of ASCII "snowflakes" to suit the chatbot's name.
     * String.repeat builds the row so the width is stated once, as a number.
     */
    private static final String BORDER = "   " + " *".repeat(30);

    /** Indentation applied to every line of message text inside a block. */
    private static final String INDENT = "     ";

    /** Prefix added to every error message shown to the user. */
    private static final String ERROR_PREFIX = "OLAF!!! ";

    /** Separates a deadline's description from the time it is due. */
    private static final String BY_SEPARATOR = "/by";

    /** Separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    // Each "\\" in the source produces a single backslash in the ASCII-art banner.
    private static final String BANNER = " _____ _           \n"
            + "|  ___| |___  __ _ \n"
            + "| |__ | / __|/ _` |\n"
            + "|  __|| \\__ \\ (_| |\n"
            + "|_____|_|___/\\__,_|";

    private static final String GREETING = "Hello! I'm Elsa.\n"
            + "Do you want to build a snowman?";

    private static final String FAREWELL = "The cold never bother me anyways!";

    public static void main(String[] args) {
        printBlock(BANNER + "\n" + GREETING);

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
                printBlock(ERROR_PREFIX + skippedLinesMessage(loaded.problems()));
            }
        } catch (ElsaException e) {
            // A file that cannot be understood is reported once, and the session goes
            // on with an empty list rather than refusing to start.
            printBlock(ERROR_PREFIX + e.getMessage());
            tasks = new ArrayList<>();
        }

        Scanner scanner = new Scanner(System.in);
        // The flag ends the loop from inside the switch, where a plain break would only
        // leave the switch. hasNextLine() guards against input ending without a "bye".
        boolean isRunning = true;
        while (isRunning && scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

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
                    printBlock(FAREWELL);
                    isRunning = false;
                }
                case LIST -> printBlock(formatTasks(tasks));
                case MARK -> {
                    int index = parseTaskIndex(arguments, tasks.size(), command);
                    tasks.get(index).markAsDone();
                    Storage.save(tasks);
                    printBlock("Nice! I've marked this task as done:\n"
                            + "  " + tasks.get(index));
                }
                case UNMARK -> {
                    int index = parseTaskIndex(arguments, tasks.size(), command);
                    tasks.get(index).markAsNotDone();
                    Storage.save(tasks);
                    printBlock("OK, I've marked this task as not done yet:\n"
                            + "  " + tasks.get(index));
                }
                case DELETE -> {
                    int index = parseTaskIndex(arguments, tasks.size(), command);
                    // remove() returns the task it took out, so it can be shown to the user.
                    Task removed = tasks.remove(index);
                    Storage.save(tasks);
                    printBlock(removedMessage(removed, tasks.size()));
                }
                case TODO -> {
                    requireDescription(arguments, command);
                    requireNoSeparator(arguments, "description of a todo", command);
                    addTask(tasks, new Todo(arguments));
                }
                case DEADLINE -> {
                    requireDescription(arguments, command);
                    // Limit of 2 keeps any later "/by" as part of the due time itself.
                    String[] parts = requireSeparator(arguments, BY_SEPARATOR, command);
                    String description = requireNonEmpty(parts[0],
                            "description of a deadline", command);
                    String by = requireNonEmpty(parts[1],
                            "due time after " + BY_SEPARATOR, command);
                    addTask(tasks, new Deadline(description, by));
                }
                case EVENT -> {
                    requireDescription(arguments, command);
                    // Split off the description first, then split what remains into the two times.
                    String[] parts = requireSeparator(arguments, FROM_SEPARATOR, command);
                    String description = requireNonEmpty(parts[0],
                            "description of an event", command);
                    String[] times = requireSeparator(parts[1], TO_SEPARATOR, command);
                    String from = requireNonEmpty(times[0],
                            "start time after " + FROM_SEPARATOR, command);
                    String to = requireNonEmpty(times[1],
                            "end time after " + TO_SEPARATOR, command);
                    addTask(tasks, new Event(description, from, to));
                }
                case NOTHING -> throw new ElsaException("You did not type anything. Try \""
                        + Command.TODO.getUsage() + "\", or \"list\" to see what you have.");
                case UNKNOWN -> throw new ElsaException(
                        "I'm sorry, but I don't know what that means :-(");
                }
            } catch (ElsaException e) {
                // One place to report anything the chatbot could not carry out.
                printBlock(ERROR_PREFIX + e.getMessage());
            }
        }
    }

    /**
     * Adds a task to the list, saves the updated list to the hard disk, and
     * confirms the addition to the user. The three commands that add a task all
     * do these same three things, so they share this method.
     *
     * @param tasks the task list to add to
     * @param task  the task the user asked to add
     * @throws ElsaException if the updated list could not be saved
     */
    private static void addTask(ArrayList<Task> tasks, Task task) throws ElsaException {
        tasks.add(task);
        // Saved before the confirmation is printed, so the chatbot never claims to
        // have stored a task that did not reach the disk.
        Storage.save(tasks);
        printBlock(addedMessage(task, tasks.size()));
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
     * Builds the warning shown when some lines of the data file could not be read.
     *
     * @param problems one message per line that could not be understood
     * @return the warning text, listing each line and what will happen to it
     */
    private static String skippedLinesMessage(ArrayList<String> problems) {
        String plural = (problems.size() == 1) ? "line" : "lines";
        StringBuilder message = new StringBuilder("I could not understand "
                + problems.size() + " " + plural + " of " + Storage.getFileName()
                + ", so I have left " + ((problems.size() == 1) ? "it" : "them")
                + " out:");
        for (String problem : problems) {
            message.append("\n  ").append(problem);
        }
        // Said plainly, because the next change to the list rewrites the file.
        message.append("\nYour other tasks loaded normally. Saving will rewrite the"
                + " file without the " + plural + " above, so edit the file now if you"
                + " want to keep " + ((problems.size() == 1) ? "it" : "them") + ".");
        return message.toString();
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
     * Builds the confirmation shown after the list has gained or lost a task.
     *
     * @param lead      the opening line saying what happened
     * @param task      the task that was added or removed
     * @param taskCount how many tasks are in the list now
     * @return the confirmation text, spanning three lines
     */
    private static String taskCountMessage(String lead, Task task, int taskCount) {
        String plural = (taskCount == 1) ? "task" : "tasks";
        return lead + "\n"
                + "  " + task + "\n"
                + "Now you have " + taskCount + " " + plural + " in the list.";
    }

    /**
     * Builds the confirmation shown after a task has been added.
     *
     * @param task      the task that was just added
     * @param taskCount how many tasks are in the list now
     * @return the confirmation text, spanning three lines
     */
    private static String addedMessage(Task task, int taskCount) {
        return taskCountMessage("Got it. I've added this task:", task, taskCount);
    }

    /**
     * Builds the confirmation shown after a task has been removed.
     *
     * @param task      the task that was just removed
     * @param taskCount how many tasks are left in the list
     * @return the confirmation text, spanning three lines
     */
    private static String removedMessage(Task task, int taskCount) {
        return taskCountMessage("Noted. I've removed this task:", task, taskCount);
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

    /**
     * Builds the numbered list of stored tasks as a single multi-line string.
     *
     * @param tasks the stored tasks, in the order they were added
     * @return a heading followed by one line per task, numbered from 1
     */
    private static String formatTasks(ArrayList<Task> tasks) {
        if (tasks.isEmpty()) {
            return "Into the Unknown.";
        }
        StringBuilder list = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            // List indices start at 0, but the display numbering starts at 1.
            // Appending the Task calls its toString() to render "[D][X] return book (by: Sunday)".
            list.append("\n").append(i + 1).append(".").append(tasks.get(i));
        }
        return list.toString();
    }

    /**
     * Prints a message enclosed between two horizontal borders,
     * indenting each line so that it lines up inside the block.
     *
     * @param message the text to display; may span several lines separated by "\n"
     */
    private static void printBlock(String message) {
        System.out.println(BORDER);
        for (String line : message.split("\n")) {
            System.out.println(INDENT + line);
        }
        System.out.println(BORDER);
        System.out.println();
    }
}
