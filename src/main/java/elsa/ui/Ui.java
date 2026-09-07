package elsa.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
     * Returns the greeting that opens a session in the window.
     *
     * <p>The banner is left out. It is drawn out of punctuation, which lines up
     * only in a font whose characters are all one width; a window's font is not,
     * so the banner would arrive crooked and is left to the terminal.
     *
     * <p>The window has no menu, so a first-time user needs telling where to
     * look. It points at "help" rather than listing the commands itself, because
     * a wall of usage lines is a poor first thing to meet and is only wanted
     * once.
     *
     * @return the opening message.
     */
    public String getGreetingMessage() {
        return GREETING + "\n\nType \"help\" to see what I can do.";
    }

    /**
     * Returns how every command is written, and how to write a date.
     *
     * <p>Four of the usage lines end in a date without saying what one looks
     * like, so the forms a date may take are named underneath. They are taken
     * from {@link Dates}, which is also what refuses a date it cannot read, so
     * the two cannot come to disagree about what is accepted.
     *
     * @param usages how each command is written, one per line
     * @return the help text.
     */
    public String getHelpMessage(List<String> usages) {
        String commands = usages.stream()
                .map(usage -> "\n  " + usage)
                .collect(Collectors.joining());
        return "Here is what you can ask me:" + commands
                + "\n\nWrite a date as " + Dates.ACCEPTED_FORMS + ".";
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
        return "Here are the tasks in your list:\n" + numberedLines(tasks, task -> true);
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
        // Each task decides for itself whether it falls on the date; see
        // Task.occursOn(), which deadlines and events answer differently.
        String list = numberedLines(tasks, task -> task.occursOn(date));
        if (list.isEmpty()) {
            return "Nothing on " + Dates.format(date) + ".";
        }
        return "Here are the tasks on " + Dates.format(date) + ":\n" + list;
    }

    /**
     * Returns the tasks whose description contains a keyword.
     *
     * <p>As in {@link #getTasksOnMessage}, each task keeps the number it has in the
     * full list rather than being renumbered from 1, so that a number read here
     * can be given straight to "mark" or "delete".
     *
     * @param tasks    the stored tasks, in the order they were added
     * @param keywords the texts being searched for, one or more
     * @return the matching tasks, or a line saying there are none.
     */
    public String getMatchingTasksMessage(TaskList tasks, String... keywords) {
        // Each task decides for itself whether it matches; see Task.matches(),
        // which searches the description only.
        String list = numberedLines(tasks, task -> task.matches(keywords));
        if (list.isEmpty()) {
            return "Nothing matching " + quoteAll(keywords) + ".";
        }
        return "Here are the matching tasks in your list:\n" + list;
    }

    /**
     * Returns the tasks a test accepts as numbered lines, one to a line, or the
     * empty string when it accepts none.
     *
     * <p>Each task keeps the number it has in the full list rather than being
     * renumbered from 1, so that a number read off a listing can be given
     * straight to "mark" or "delete". That is why the stream runs over the
     * positions rather than over the tasks: the position is what has to survive
     * the filtering.
     *
     * <p>Returning the empty string for "nothing was accepted" saves the flag a
     * caller would otherwise keep and set inside a loop, because a numbered line
     * is never itself empty.
     *
     * @param tasks    the stored tasks, in the order they were added
     * @param isWanted decides whether a task belongs in the listing.
     * @return the numbered lines joined by newlines, or the empty string
     */
    private static String numberedLines(TaskList tasks, Predicate<Task> isWanted) {
        return IntStream.range(0, tasks.size())
                .filter(i -> isWanted.test(tasks.get(i)))
                .mapToObj(i -> numbered(i, tasks.get(i)))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Returns the keywords in quotation marks, run together as English rather
     * than as a list, so that the complaint reads as a sentence: one on its own,
     * two joined by "or", and more than two separated by commas until the last.
     *
     * @param keywords the texts that were searched for
     * @return the keywords quoted and joined
     */
    private static String quoteAll(String... keywords) {
        StringBuilder quoted = new StringBuilder();
        for (int i = 0; i < keywords.length; i++) {
            if (i > 0) {
                quoted.append(i == keywords.length - 1 ? " or " : ", ");
            }
            quoted.append("\"").append(keywords[i]).append("\"");
        }
        return quoted.toString();
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
        String listed = problems.stream()
                .map(problem -> "\n  " + problem)
                .collect(Collectors.joining());
        // Said plainly, because the next change to the list rewrites the file.
        String message = "I could not understand " + problems.size() + " " + plural
                + " of " + fileName + ", so I have left " + them + " out:" + listed
                + "\nYour other tasks loaded normally. Saving will rewrite the file"
                + " without the " + plural + " above, so edit the file now if you"
                + " want to keep " + them + ".";
        return getErrorMessage(message);
    }

    /**
     * Returns one numbered line of a task listing.
     * Written as a function from a task to its line, rather than as something
     * that adds to a list being built, so that a listing can map over the tasks
     * it wants and let a collector join what comes back.
     *
     * @param index the task's position in the full list, counted from 0
     * @param task  the task to show
     * @return the display number, a full stop, and the task
     */
    private static String numbered(int index, Task task) {
        // List indices start at 0, but the display numbering starts at 1.
        return (index + 1) + "." + task;
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
