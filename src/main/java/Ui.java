import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Everything the chatbot says to the user and reads back from them.
 *
 * <p>This class is the only one that writes to the screen or reads from the
 * keyboard. Keeping that in one place means the rest of the program decides
 * <em>what</em> has happened and leaves this class to decide <em>how</em> it is
 * worded and laid out, so a change to the wording, the borders or the greeting
 * is made here and nowhere else.
 */
public class Ui {
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

    // Each "\\" in the source produces a single backslash in the ASCII-art banner.
    private static final String BANNER = " _____ _           \n"
            + "|  ___| |___  __ _ \n"
            + "| |__ | / __|/ _` |\n"
            + "|  __|| \\__ \\ (_| |\n"
            + "|_____|_|___/\\__,_|";

    private static final String GREETING = "Hello! I'm Elsa.\n"
            + "Do you want to build a snowman?";

    private static final String FAREWELL = "The cold never bother me anyways!";

    /** Shown in place of the list when there is nothing in it. */
    private static final String EMPTY_LIST = "Into the Unknown.";

    /**
     * Where the user's typing is read from. Held as a field so that the one
     * Scanner lasts for the whole session; a new one per line would work but
     * would keep opening a fresh reader on the same stream.
     */
    private final Scanner scanner;

    /** Creates a user interface that reads from the keyboard and writes to the screen. */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Returns whether the user has typed another line.
     * This is false when the input ends without a "bye", which happens when the
     * chatbot is fed a file of commands rather than being typed at.
     *
     * @return true if there is another line to read
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next line the user typed, without the spaces around it.
     *
     * @return the line, trimmed
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Shows the banner and greeting that open a session. */
    public void showWelcome() {
        showBlock(BANNER + "\n" + GREETING);
    }

    /** Shows the parting message. */
    public void showFarewell() {
        showBlock(FAREWELL);
    }

    /**
     * Shows something the chatbot could not do, marked as a complaint.
     *
     * @param message the explanation, written for the user
     */
    public void showError(String message) {
        showBlock(ERROR_PREFIX + message);
    }

    /**
     * Shows the confirmation after a task has been added.
     *
     * @param task      the task that was just added
     * @param taskCount how many tasks are in the list now
     */
    public void showAdded(Task task, int taskCount) {
        showBlock(taskCountMessage("Got it. I've added this task:", task, taskCount));
    }

    /**
     * Shows the confirmation after a task has been removed.
     *
     * @param task      the task that was just removed
     * @param taskCount how many tasks are left in the list
     */
    public void showRemoved(Task task, int taskCount) {
        showBlock(taskCountMessage("Noted. I've removed this task:", task, taskCount));
    }

    /**
     * Shows the confirmation after a task has been marked done.
     *
     * @param task the task that was marked
     */
    public void showMarked(Task task) {
        showBlock("Nice! I've marked this task as done:\n  " + task);
    }

    /**
     * Shows the confirmation after a task has been marked not done.
     *
     * @param task the task that was unmarked
     */
    public void showUnmarked(Task task) {
        showBlock("OK, I've marked this task as not done yet:\n  " + task);
    }

    /**
     * Shows the whole task list, numbered from 1.
     *
     * @param tasks the stored tasks, in the order they were added
     */
    public void showTasks(TaskList tasks) {
        if (tasks.isEmpty()) {
            showBlock(EMPTY_LIST);
            return;
        }
        StringBuilder list = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            appendNumbered(list, i, tasks.get(i));
        }
        showBlock(list.toString());
    }

    /**
     * Shows the tasks falling on one date.
     *
     * <p>Each task keeps the number it has in the full list rather than being
     * renumbered from 1, so that a number read here can be given straight to
     * "mark" or "delete". Renumbering would make those commands act on the wrong
     * task, because they count positions in the whole list.
     *
     * @param tasks the stored tasks, in the order they were added
     * @param date  the date being asked about
     */
    public void showTasksOn(TaskList tasks, LocalDate date) {
        StringBuilder list = new StringBuilder("Here are the tasks on "
                + Dates.format(date) + ":");
        boolean isFound = false;
        for (int i = 0; i < tasks.size(); i++) {
            // Each task decides for itself whether it falls on the date; see
            // Task.occursOn(), which deadlines and events answer differently.
            if (tasks.get(i).occursOn(date)) {
                isFound = true;
                appendNumbered(list, i, tasks.get(i));
            }
        }
        if (!isFound) {
            showBlock("Nothing on " + Dates.format(date) + ".");
            return;
        }
        showBlock(list.toString());
    }

    /**
     * Shows a warning naming the lines of the data file that could not be read.
     *
     * @param problems one message per line that could not be understood
     */
    public void showSkippedLines(ArrayList<String> problems) {
        String plural = (problems.size() == 1) ? "line" : "lines";
        String them = (problems.size() == 1) ? "it" : "them";
        StringBuilder message = new StringBuilder("I could not understand "
                + problems.size() + " " + plural + " of " + Storage.getFileName()
                + ", so I have left " + them + " out:");
        for (String problem : problems) {
            message.append("\n  ").append(problem);
        }
        // Said plainly, because the next change to the list rewrites the file.
        message.append("\nYour other tasks loaded normally. Saving will rewrite the"
                + " file without the " + plural + " above, so edit the file now if you"
                + " want to keep " + them + ".");
        showError(message.toString());
    }

    /**
     * Adds one numbered line to a list being built.
     *
     * @param list  the list being built
     * @param index the task's position in the full list, counted from 0
     * @param task  the task to add
     */
    private static void appendNumbered(StringBuilder list, int index, Task task) {
        // List indices start at 0, but the display numbering starts at 1.
        // Appending the Task calls its toString() to render "[D][X] return book".
        list.append("\n").append(index + 1).append(".").append(task);
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
     * Prints a message enclosed between two horizontal borders,
     * indenting each line so that it lines up inside the block.
     *
     * @param message the text to display; may span several lines separated by "\n"
     */
    private void showBlock(String message) {
        System.out.println(BORDER);
        for (String line : message.split("\n")) {
            System.out.println(INDENT + line);
        }
        System.out.println(BORDER);
        System.out.println();
    }
}
