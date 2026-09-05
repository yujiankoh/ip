package elsa.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import elsa.Dates;
import elsa.task.Task;
import elsa.task.TaskList;

/**
 * Everything the chatbot says to the user and reads back from them.
 *
 * <p>This class is the only one that words what the chatbot says. The rest of
 * the program decides <em>what</em> has happened and leaves this class to decide
 * <em>how</em> it is put, so a change to the wording, the borders or the
 * greeting is made here and nowhere else.
 *
 * <p>Wording a message and showing it are separate. Every {@code get...Message}
 * method returns text and displays nothing, because the chatbot is spoken to
 * through two faces: a terminal, which prints the text between borders through
 * {@link #show}, and a window, which puts the same text in a dialog box. Only
 * the terminal reads from the keyboard, so {@link #readCommand} stays here too.
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

    /**
     * Returns the banner and greeting that open a session in the terminal.
     *
     * @return the opening message, banner first.
     */
    public String getWelcomeMessage() {
        return BANNER + "\n" + GREETING;
    }

    /**
     * Returns the greeting that opens a session in the window: who the chatbot
     * is, and what it can be asked to do.
     *
     * <p>The banner is left out. It is drawn out of punctuation, which lines up
     * only in a font whose characters are all one width; a window's font is not,
     * so the banner would arrive crooked and is left to the terminal.
     *
     * <p>The window has no menu and no prompt, so an empty conversation would
     * leave a first-time user with nothing to go on. Listing the commands here
     * is the window's answer to that.
     *
     * <p>The lines are handed in rather than looked up, because this package is
     * already depended on by the one that knows them and must not depend on it
     * back.
     *
     * <p>Four of those lines end in a date without saying what one looks like,
     * so the forms a date may be written in are named underneath. They are taken
     * from {@link Dates}, which is also what refuses a date it cannot read, so
     * the two cannot come to disagree about what is accepted.
     *
     * @param usages how each command is written, one per line
     * @return the opening message.
     */
    public String getGreetingMessage(List<String> usages) {
        StringBuilder message = new StringBuilder(GREETING);
        message.append("\n\nHere is what you can ask me:");
        for (String usage : usages) {
            message.append("\n  ").append(usage);
        }
        message.append("\n\nWrite a date as ").append(Dates.ACCEPTED_FORMS).append(".");
        return message.toString();
    }

    /**
     * Returns the parting message.
     *
     * @return what the chatbot says as the session ends.
     */
    public String getFarewellMessage() {
        return FAREWELL;
    }

    /**
     * Returns something the chatbot could not do, marked as a complaint.
     *
     * @param message the explanation, written for the user
     * @return the explanation behind the chatbot's complaint prefix.
     */
    public String getErrorMessage(String message) {
        return ERROR_PREFIX + message;
    }

    /**
     * Returns the confirmation shown after a task has been added.
     *
     * @param task      the task that was just added
     * @param taskCount how many tasks are in the list now
     * @return the confirmation text.
     */
    public String getAddedMessage(Task task, int taskCount) {
        return taskCountMessage("Got it. I've added this task:", task, taskCount);
    }

    /**
     * Returns the confirmation shown after a task has been removed.
     *
     * @param task      the task that was just removed
     * @param taskCount how many tasks are left in the list
     * @return the confirmation text.
     */
    public String getRemovedMessage(Task task, int taskCount) {
        return taskCountMessage("Noted. I've removed this task:", task, taskCount);
    }

    /**
     * Returns the confirmation shown after a task has been marked done.
     *
     * @param task the task that was marked
     * @return the confirmation text.
     */
    public String getMarkedMessage(Task task) {
        return "Nice! I've marked this task as done:\n  " + task;
    }

    /**
     * Returns the confirmation shown after a task has been marked not done.
     *
     * @param task the task that was unmarked
     * @return the confirmation text.
     */
    public String getUnmarkedMessage(Task task) {
        return "OK, I've marked this task as not done yet:\n  " + task;
    }

    /**
     * Returns the whole task list, numbered from 1.
     *
     * @param tasks the stored tasks, in the order they were added
     * @return the numbered list, or a stand-in line if there is nothing in it.
     */
    public String getTasksMessage(TaskList tasks) {
        if (tasks.isEmpty()) {
            return EMPTY_LIST;
        }
        StringBuilder list = new StringBuilder("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            appendNumbered(list, i, tasks.get(i));
        }
        return list.toString();
    }

    /**
     * Returns the tasks falling on one date.
     *
     * <p>Each task keeps the number it has in the full list rather than being
     * renumbered from 1, so that a number read here can be given straight to
     * "mark" or "delete". Renumbering would make those commands act on the wrong
     * task, because they count positions in the whole list.
     *
     * @param tasks the stored tasks, in the order they were added
     * @param date  the date being asked about
     * @return the matching tasks, or a line saying there are none.
     */
    public String getTasksOnMessage(TaskList tasks, LocalDate date) {
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
            return "Nothing on " + Dates.format(date) + ".";
        }
        return list.toString();
    }

    /**
     * Returns the tasks whose description contains a keyword.
     *
     * <p>As in {@link #getTasksOnMessage}, each task keeps the number it has in the
     * full list rather than being renumbered from 1, so that a number read here
     * can be given straight to "mark" or "delete".
     *
     * @param tasks   the stored tasks, in the order they were added
     * @param keyword the text being searched for
     * @return the matching tasks, or a line saying there are none.
     */
    public String getMatchingTasksMessage(TaskList tasks, String keyword) {
        StringBuilder list = new StringBuilder("Here are the matching tasks in your list:");
        boolean isFound = false;
        for (int i = 0; i < tasks.size(); i++) {
            // Each task decides for itself whether it matches; see Task.matches(),
            // which searches the description only.
            if (tasks.get(i).matches(keyword)) {
                isFound = true;
                appendNumbered(list, i, tasks.get(i));
            }
        }
        if (!isFound) {
            return "Nothing matching \"" + keyword + "\".";
        }
        return list.toString();
    }

    /**
     * Returns a warning naming the lines of the data file that could not be read.
     *
     * @param problems one message per line that could not be understood
     * @param fileName the file the lines came from, named so the user can go and fix it
     * @return the warning, worded as a complaint.
     */
    public String getSkippedLinesMessage(ArrayList<String> problems, String fileName) {
        String plural = (problems.size() == 1) ? "line" : "lines";
        String them = (problems.size() == 1) ? "it" : "them";
        StringBuilder message = new StringBuilder("I could not understand "
                + problems.size() + " " + plural + " of " + fileName
                + ", so I have left " + them + " out:");
        for (String problem : problems) {
            message.append("\n  ").append(problem);
        }
        // Said plainly, because the next change to the list rewrites the file.
        message.append("\nYour other tasks loaded normally. Saving will rewrite the"
                + " file without the " + plural + " above, so edit the file now if you"
                + " want to keep " + them + ".");
        return getErrorMessage(message.toString());
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
     * Prints a message enclosed between two horizontal borders, indenting each
     * line so that it lines up inside the block.
     * Only the terminal shows messages this way. The window draws its own border
     * round each dialog box, and is handed the same text undecorated.
     *
     * @param message the text to display; may span several lines separated by "\n"
     */
    public void show(String message) {
        System.out.println(BORDER);
        for (String line : message.split("\n")) {
            System.out.println(INDENT + line);
        }
        System.out.println(BORDER);
        System.out.println();
    }
}
