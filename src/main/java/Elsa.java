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
    private static final String ERROR_PREFIX = "OOPS!!! ";

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

    /** Largest number of items that can be stored, per the Level-2 assumption. */
    private static final int MAX_TASKS = 100;

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

        // The array is fixed at MAX_TASKS slots, but only the first taskCount of them
        // hold real values, so taskCount is tracked separately from tasks.length.
        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

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
                    printBlock(formatTasks(tasks, taskCount));
                } else if (keyword.equals(MARK_COMMAND)) {
                    int index = parseTaskIndex(arguments);
                    tasks[index].markAsDone();
                    printBlock("Nice! I've marked this task as done:\n"
                            + "  " + tasks[index]);
                } else if (keyword.equals(UNMARK_COMMAND)) {
                    int index = parseTaskIndex(arguments);
                    tasks[index].markAsNotDone();
                    printBlock("OK, I've marked this task as not done yet:\n"
                            + "  " + tasks[index]);
                } else if (keyword.equals(TODO_COMMAND)) {
                    requireDescription(arguments, TODO_COMMAND);
                    tasks[taskCount] = new Todo(arguments);
                    taskCount++;
                    printBlock(addedMessage(tasks[taskCount - 1], taskCount));
                } else if (keyword.equals(DEADLINE_COMMAND)) {
                    requireDescription(arguments, DEADLINE_COMMAND);
                    // Limit of 2 keeps any later "/by" as part of the due time itself.
                    String[] parts = arguments.split(BY_SEPARATOR, 2);
                    tasks[taskCount] = new Deadline(parts[0].trim(), parts[1].trim());
                    taskCount++;
                    printBlock(addedMessage(tasks[taskCount - 1], taskCount));
                } else if (keyword.equals(EVENT_COMMAND)) {
                    requireDescription(arguments, EVENT_COMMAND);
                    // Split off the description first, then split what remains into the two times.
                    String[] parts = arguments.split(FROM_SEPARATOR, 2);
                    String[] times = parts[1].split(TO_SEPARATOR, 2);
                    tasks[taskCount] = new Event(parts[0].trim(), times[0].trim(), times[1].trim());
                    taskCount++;
                    printBlock(addedMessage(tasks[taskCount - 1], taskCount));
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
    private static void requireDescription(String arguments, String keyword) throws ElsaException {
        if (arguments.isEmpty()) {
            // "an event" but "a todo": pick the article that reads correctly.
            String article = ("aeiou".indexOf(keyword.charAt(0)) >= 0) ? "an" : "a";
            throw new ElsaException("The description of " + article + " " + keyword
                    + " cannot be empty.");
        }
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
     * Converts the task number typed by the user into an array index.
     *
     * @param arguments everything the user typed after the command keyword
     * @return the corresponding 0-based index into the task array
     */
    private static int parseTaskIndex(String arguments) {
        // The user counts from 1, so subtract 1 to get the array index.
        return Integer.parseInt(arguments) - 1;
    }

    /**
     * Builds the numbered list of stored tasks as a single multi-line string.
     *
     * @param tasks     array holding the stored tasks
     * @param taskCount number of slots at the front of the array that are in use
     * @return a heading followed by one line per task, numbered from 1
     */
    private static String formatTasks(Task[] tasks, int taskCount) {
        if (taskCount == 0) {
            return "Into the Unknown.";
        }
        StringBuilder list = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            // Array indices start at 0, but the display numbering starts at 1.
            // Appending the Task calls its toString() to render "[D][X] return book (by: Sunday)".
            list.append("\n").append(i + 1).append(".").append(tasks[i]);
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
