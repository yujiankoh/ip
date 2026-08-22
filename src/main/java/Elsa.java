import java.util.Scanner;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores whatever text the user enters, lists it back on request,
 * marks tasks as done, and exits when the user types "bye".
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
        String[] tasks = new String[MAX_TASKS];
        // Runs in parallel with tasks: isDone[i] is the done flag for tasks[i].
        // Two arrays rather than one Task class, since this increment forbids new classes.
        boolean[] isDone = new boolean[MAX_TASKS];
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
                printBlock(formatTasks(tasks, isDone, taskCount));
            } else if (command.startsWith(MARK_COMMAND + " ")) {
                // The user counts from 1, so subtract 1 to get the array index.
                int index = Integer.parseInt(command.substring(MARK_COMMAND.length() + 1).trim()) - 1;
                isDone[index] = true;
                printBlock("Nice! I've marked this task as done:\n"
                        + "  " + formatTask(tasks[index], isDone[index]));
            } else {
                tasks[taskCount] = command;
                taskCount++;
                printBlock("added: " + command);
            }
        }
    }

    /**
     * Formats one task as its status icon followed by its description,
     * for example "[X] read book".
     *
     * @param description the task's text
     * @param done        whether the task has been marked as done
     * @return the task rendered as a single line, without any list number
     */
    private static String formatTask(String description, boolean done) {
        // A conditional expression: picks the first value when done is true, else the second.
        String statusIcon = done ? "[X]" : "[ ]";
        return statusIcon + " " + description;
    }

    /**
     * Builds the numbered list of stored tasks as a single multi-line string.
     *
     * @param tasks     array holding the stored tasks
     * @param isDone    array holding the done flag for each stored task
     * @param taskCount number of slots at the front of the arrays that are in use
     * @return a heading followed by one line per task, numbered from 1
     */
    private static String formatTasks(String[] tasks, boolean[] isDone, int taskCount) {
        if (taskCount == 0) {
            return "Into the Unknown.";
        }
        StringBuilder list = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            // Array indices start at 0, but the display numbering starts at 1.
            list.append("\n").append(i + 1).append(".").append(formatTask(tasks[i], isDone[i]));
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
