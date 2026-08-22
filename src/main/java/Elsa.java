import java.util.Scanner;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores whatever text the user enters, lists it back on request,
 * marks tasks as done or not done, and exits when the user types "bye".
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
            String command = scanner.nextLine();
            if (command.equals(EXIT_COMMAND)) {
                printBlock(FAREWELL);
                break;
            } else if (command.equals(LIST_COMMAND)) {
                printBlock(formatTasks(tasks, taskCount));
            } else if (command.startsWith(MARK_COMMAND + " ")) {
                int index = parseTaskIndex(command, MARK_COMMAND);
                tasks[index].markAsDone();
                printBlock("Nice! I've marked this task as done:\n"
                        + "  " + tasks[index]);
            } else if (command.startsWith(UNMARK_COMMAND + " ")) {
                int index = parseTaskIndex(command, UNMARK_COMMAND);
                tasks[index].markAsNotDone();
                printBlock("OK, I've marked this task as not done yet:\n"
                        + "  " + tasks[index]);
            } else if (command.startsWith(TODO_COMMAND + " ")) {
                String description = command.substring(TODO_COMMAND.length() + 1).trim();
                tasks[taskCount] = new Todo(description);
                taskCount++;
                printBlock(addedMessage(tasks[taskCount - 1], taskCount));
            } else if (command.startsWith(DEADLINE_COMMAND + " ")) {
                String arguments = command.substring(DEADLINE_COMMAND.length() + 1);
                // Limit of 2 keeps any later "/by" as part of the due time itself.
                String[] parts = arguments.split(BY_SEPARATOR, 2);
                String description = parts[0].trim();
                String by = parts[1].trim();
                tasks[taskCount] = new Deadline(description, by);
                taskCount++;
                printBlock(addedMessage(tasks[taskCount - 1], taskCount));
            } else if (command.startsWith(EVENT_COMMAND + " ")) {
                String arguments = command.substring(EVENT_COMMAND.length() + 1);
                // Split off the description first, then split what remains into the two times.
                String[] parts = arguments.split(FROM_SEPARATOR, 2);
                String description = parts[0].trim();
                String[] times = parts[1].split(TO_SEPARATOR, 2);
                String from = times[0].trim();
                String to = times[1].trim();
                tasks[taskCount] = new Event(description, from, to);
                taskCount++;
                printBlock(addedMessage(tasks[taskCount - 1], taskCount));
            } else {
                // Plain text with no command keyword still adds a task, as in Level-2.
                // This fallback goes away once unknown input becomes an error.
                tasks[taskCount] = new Task(command);
                taskCount++;
                printBlock("added: " + command);
            }
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
     * Reads the task number that follows a command keyword and converts it to an array index.
     *
     * @param command the full line typed by the user, for example "unmark 2"
     * @param keyword the command keyword at the start of that line, for example "unmark"
     * @return the corresponding 0-based index into the task arrays
     */
    private static int parseTaskIndex(String command, String keyword) {
        String argument = command.substring(keyword.length() + 1).trim();
        // The user counts from 1, so subtract 1 to get the array index.
        return Integer.parseInt(argument) - 1;
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
            // Appending the Task calls its toString() to render "[X] read book".
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
