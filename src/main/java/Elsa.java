import java.util.Scanner;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores whatever text the user enters, lists it back on request,
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

    /** Command that ends the conversation. */
    private static final String EXIT_COMMAND = "bye";

    /** Command that displays everything stored so far. */
    private static final String LIST_COMMAND = "list";

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
            } else {
                tasks[taskCount] = command;
                taskCount++;
                printBlock("added: " + command);
            }
        }
    }

    /**
     * Builds the numbered list of stored tasks as a single multi-line string.
     *
     * @param tasks     array holding the stored tasks
     * @param taskCount number of slots at the front of the array that are in use
     * @return one line per task, numbered from 1
     */
    private static String formatTasks(String[] tasks, int taskCount) {
        if (taskCount == 0) {
            return "Into the Unknown.";
        }
        StringBuilder list = new StringBuilder();
        for (int i = 0; i < taskCount; i++) {
            if (i > 0) {
                list.append("\n");
            }
            // Array indices start at 0, but the display numbering starts at 1.
            list.append(i + 1).append(". ").append(tasks[i]);
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
