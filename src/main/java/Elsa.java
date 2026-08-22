import java.util.ArrayList;
import java.util.Scanner;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores todos, deadlines and events, lists them back on request,
 * marks them as done or not done, reports what it cannot understand,
 * and exits when the user types "bye".
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

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /** Command that displays everything stored so far. */
    private static final String LIST_COMMAND = "list";

    /** Command that marks a task as done; followed by the task's list number. */
    private static final String MARK_COMMAND = "mark";

    /** Command that marks a task as not done again; followed by the task's list number. */
    private static final String UNMARK_COMMAND = "unmark";

    /** Command that adds a todo; followed by the task's description. */
    private static final String TODO_COMMAND = "todo";

    /** Command that adds a deadline; followed by a description and "/by <when>". */
    private static final String DEADLINE_COMMAND = "deadline";

    /** Separates a deadline's description from the time it is due. */
    private static final String BY_SEPARATOR = "/by";

    /** Command that adds an event; followed by a description, "/from <start>" and "/to <end>". */
    private static final String EVENT_COMMAND = "event";

    /** Separates an event's description from its start time. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start time from its end time. */
    private static final String TO_SEPARATOR = "/to";

    /** Shown to the user as the correct way to type each command. */
    private static final String TODO_USAGE = "todo <description>";
    private static final String DEADLINE_USAGE = "deadline <description> /by <when>";
    private static final String EVENT_USAGE = "event <description> /from <start> /to <end>";

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
        ArrayList<Task> tasks = new ArrayList<>();

        Scanner scanner = new Scanner(System.in);
        // hasNextLine() guards against the input stream ending without a "bye",
        // which would otherwise make nextLine() throw.
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            // Every line is one keyword plus whatever follows it. Splitting here means
            // "todo" with nothing after it is recognised as a todo missing its description,
            // rather than being mistaken for an unknown command.
            String[] words = command.split(" ", 2);
            String keyword = words[0];
            String arguments = (words.length > 1) ? words[1].trim() : "";

            try {
                if (keyword.equals(EXIT_COMMAND)) {
                    printBlock(FAREWELL);
                    break;
                } else if (keyword.equals(LIST_COMMAND)) {
                    printBlock(formatTasks(tasks));
                } else if (keyword.equals(MARK_COMMAND)) {
                    int index = parseTaskIndex(arguments, tasks.size(), MARK_COMMAND);
                    tasks.get(index).markAsDone();
                    printBlock("Nice! I've marked this task as done:\n"
                            + "  " + tasks.get(index));
                } else if (keyword.equals(UNMARK_COMMAND)) {
                    int index = parseTaskIndex(arguments, tasks.size(), UNMARK_COMMAND);
                    tasks.get(index).markAsNotDone();
                    printBlock("OK, I've marked this task as not done yet:\n"
                            + "  " + tasks.get(index));
                } else if (keyword.equals(TODO_COMMAND)) {
                    requireDescription(arguments, TODO_COMMAND, TODO_USAGE);
                    Task added = new Todo(arguments);
                    tasks.add(added);
                    printBlock(addedMessage(added, tasks.size()));
                } else if (keyword.equals(DEADLINE_COMMAND)) {
                    requireDescription(arguments, DEADLINE_COMMAND, DEADLINE_USAGE);
                    // Limit of 2 keeps any later "/by" as part of the due time itself.
                    String[] parts = requireSeparator(arguments, BY_SEPARATOR, DEADLINE_USAGE);
                    String description = requireNonEmpty(parts[0],
                            "description of a deadline", DEADLINE_USAGE);
                    String by = requireNonEmpty(parts[1],
                            "due time after " + BY_SEPARATOR, DEADLINE_USAGE);
                    Task added = new Deadline(description, by);
                    tasks.add(added);
                    printBlock(addedMessage(added, tasks.size()));
                } else if (keyword.equals(EVENT_COMMAND)) {
                    requireDescription(arguments, EVENT_COMMAND, EVENT_USAGE);
                    // Split off the description first, then split what remains into the two times.
                    String[] parts = requireSeparator(arguments, FROM_SEPARATOR, EVENT_USAGE);
                    String description = requireNonEmpty(parts[0],
                            "description of an event", EVENT_USAGE);
                    String[] times = requireSeparator(parts[1], TO_SEPARATOR, EVENT_USAGE);
                    String from = requireNonEmpty(times[0],
                            "start time after " + FROM_SEPARATOR, EVENT_USAGE);
                    String to = requireNonEmpty(times[1],
                            "end time after " + TO_SEPARATOR, EVENT_USAGE);
                    Task added = new Event(description, from, to);
                    tasks.add(added);
                    printBlock(addedMessage(added, tasks.size()));
                } else if (keyword.isEmpty()) {
                    throw new ElsaException("You did not type anything. Try \"todo <description>\", "
                            + "or \"list\" to see what you have.");
                } else {
                    throw new ElsaException("I'm sorry, but I don't know what that means :-(");
                }
            } catch (ElsaException e) {
                // One place to report anything the chatbot could not carry out.
                printBlock(ERROR_PREFIX + e.getMessage());
            }
        }
    }

    /**
     * Checks that a command that adds a task was given something to add.
     *
     * @param arguments everything the user typed after the command keyword
     * @param keyword   the command keyword, used to name the kind of task in the message
     * @throws ElsaException if nothing was typed after the keyword
     */
    private static void requireDescription(String arguments, String keyword, String usage)
            throws ElsaException {
        if (arguments.isEmpty()) {
            // "an event" but "a todo": pick the article that reads correctly.
            String article = ("aeiou".indexOf(keyword.charAt(0)) >= 0) ? "an" : "a";
            throw new ElsaException("The description of " + article + " " + keyword
                    + " cannot be empty. Use: " + usage);
        }
    }


    /**
     * Splits text on a separator the command requires, reporting its absence to the user.
     *
     * @param text      the text to split
     * @param separator the separator the command cannot do without, such as "/by"
     * @param usage     how the command should be typed, shown if the separator is missing
     * @return the two pieces on either side of the first occurrence of the separator
     * @throws ElsaException if the separator does not appear in the text
     */
    private static String[] requireSeparator(String text, String separator, String usage)
            throws ElsaException {
        // Limit of 2 keeps any later occurrence as part of the second piece.
        String[] parts = text.split(separator, 2);
        if (parts.length < 2) {
            throw new ElsaException("I could not find \"" + separator + "\" in that. Use: " + usage);
        }
        return parts;
    }

    /**
     * Checks that a piece of a command was actually filled in.
     *
     * @param value the piece to check, before trimming
     * @param what  what the piece is, named for the error message
     * @param usage how the command should be typed, shown if the piece is missing
     * @return the value with surrounding spaces removed
     * @throws ElsaException if the piece is empty once trimmed
     */
    private static String requireNonEmpty(String value, String what, String usage)
            throws ElsaException {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ElsaException("The " + what + " cannot be empty. Use: " + usage);
        }
        return trimmed;
    }

    /**
     * Builds the confirmation shown after a task has been added.
     *
     * @param task      the task that was just added
     * @param taskCount how many tasks are in the list now
     * @return the confirmation text, spanning three lines
     */
    private static String addedMessage(Task task, int taskCount) {
        String plural = (taskCount == 1) ? "task" : "tasks";
        return "Got it. I've added this task:\n"
                + "  " + task + "\n"
                + "Now you have " + taskCount + " " + plural + " in the list.";
    }

    /**
     * Converts the task number typed by the user into an array index,
     * checking that it is a whole number and that a task with that number exists.
     *
     * @param arguments everything the user typed after the command keyword
     * @param taskCount how many tasks are stored, so the number can be range checked
     * @param keyword   the command keyword, used to word the error messages
     * @return the corresponding 0-based index into the task list
     * @throws ElsaException if no number was given, it is not a whole number,
     *                       or no task has that number
     */
    private static int parseTaskIndex(String arguments, int taskCount, String keyword)
            throws ElsaException {
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
                    + keyword + ". Add one with \"" + TODO_USAGE + "\" first.");
        }
        if (number < 1 || number > taskCount) {
            String plural = (taskCount == 1) ? "task" : "tasks";
            throw new ElsaException("There is no task " + number + ". You have "
                    + taskCount + " " + plural + ", so use a number from 1 to " + taskCount + ".");
        }

        // The user counts from 1, so subtract 1 to get the array index.
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
            // Array indices start at 0, but the display numbering starts at 1.
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
