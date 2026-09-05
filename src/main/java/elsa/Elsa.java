package elsa;

import elsa.command.Command;
import elsa.parser.Parser;
import elsa.storage.Storage;
import elsa.task.TaskList;
import elsa.ui.Ui;

/**
 * Entry point of the Elsa chatbot.
 * Greets the user, stores todos, deadlines and events, lists them back on request,
 * marks them as done or not done, deletes them, lists those falling on a given
 * date, reports what it cannot understand,
 * and exits when the user types "bye".
 * The task list is saved to the hard disk every time it changes and is read back
 * at startup; see {@link Storage}.
 *
 * <p>The work is shared out: {@link Ui} handles what the user sees and types,
 * {@link Parser} works out what a typed line means, {@link TaskList} holds the
 * tasks, and {@link Storage} keeps them on the disk. What is left here is the
 * order those happen in, which is the one thing that has to know about them all.
 */
public class Elsa {
    /** Where the tasks are kept between runs, relative to the project root. */
    private static final String FILE_PATH = "data/elsa.txt";

    /** What the user sees and types. Never replaced once the session starts. */
    private final Ui ui;

    /** Reads and writes the saved tasks. */
    private final Storage storage;

    /**
     * The tasks being kept. Not final because it is replaced when the saved list
     * is read at startup, and again if that list turns out to be unreadable.
     */
    private TaskList tasks;

    /**
     * Whether the user has said goodbye.
     * The terminal learns this from the command it just ran, but the window only
     * gets the reply text back, so the answer is recorded here for it to ask
     * afterwards through {@link #isExiting()}.
     */
    private boolean isExiting = false;

    /**
     * Creates a chatbot that keeps its tasks in the named file, with nothing in
     * its list yet.
     * Reading the saved tasks is left to {@link #run()} rather than done here, so
     * that the greeting is shown before any complaint about the saved file, and
     * so that making a chatbot does not by itself touch the disk.
     *
     * @param filePath where the tasks are kept, relative to where the program is run
     */
    public Elsa(String filePath) {
        this.ui = new Ui();
        this.storage = new Storage(filePath);
        this.tasks = new TaskList();
    }

    /**
     * Creates a chatbot that keeps its tasks where this program usually keeps
     * them.
     * There are two ways to start the chatbot, the terminal and the window, and
     * both want the same file. Naming it once here means neither has to know the
     * path, and moving the file later is one change rather than two.
     */
    public Elsa() {
        this(FILE_PATH);
    }

    /**
     * Returns what the chatbot says before the user has typed anything, having
     * read the tasks saved by an earlier run.
     *
     * <p>This is the window's counterpart to the opening of {@link #run()}. The
     * terminal shows the greeting and any complaint about the saved file as two
     * separate blocks; the window has one dialog box to put them in, so they are
     * joined here with a blank line between them.
     *
     * @return the chatbot's opening message.
     */
    public String startSession() {
        String loadReport = loadTasks();
        String greeting = ui.getGreetingMessage();
        return loadReport.isEmpty() ? greeting : greeting + "\n\n" + loadReport;
    }

    /**
     * Returns what the chatbot says in reply to a line the user typed.
     *
     * <p>This is what the window calls for each line sent, and it does the same
     * work as one turn of the loop in {@link #run()}: read the line, carry out
     * what it asks, and say what happened. A line the chatbot cannot carry out
     * is answered with the complaint rather than thrown, because the window has
     * nowhere to throw it to and the user is owed an answer either way.
     *
     * @param input the line the user typed.
     * @return what the chatbot says back.
     */
    public String getResponse(String input) {
        try {
            Command command = Parser.parse(input.trim());
            String response = command.execute(tasks, ui, storage);
            isExiting = command.isExit();
            return response;
        } catch (ElsaException e) {
            return ui.getErrorMessage(e.getMessage());
        }
    }

    /**
     * Returns whether the user has said goodbye.
     *
     * @return true once a command has ended the session.
     */
    public boolean isExiting() {
        return isExiting;
    }

    /**
     * Runs one session: greets the user, reads the saved tasks, then carries out
     * commands until the user says "bye" or the input ends.
     */
    public void run() {
        ui.show(ui.getWelcomeMessage());

        // Shown as its own block, after the greeting, so that a complaint about
        // the saved file does not arrive mixed into the welcome.
        String loadReport = loadTasks();
        if (!loadReport.isEmpty()) {
            ui.show(loadReport);
        }

        // Each command says whether the session should end, so the loop does not
        // need to know which one means goodbye. hasNextCommand() guards against
        // input ending without a "bye".
        while (!isExiting && ui.hasNextCommand()) {
            try {
                Command command = Parser.parse(ui.readCommand());
                ui.show(command.execute(tasks, ui, storage));
                isExiting = command.isExit();
            } catch (ElsaException e) {
                // One place to report anything the chatbot could not carry out.
                ui.show(ui.getErrorMessage(e.getMessage()));
            }
        }
    }

    /**
     * Reads the tasks saved by an earlier run into the list, and returns anything
     * the user should be told about the reading.
     * On the very first run there is no file yet, and load() returns an empty
     * list rather than treating that as a problem.
     *
     * @return what to tell the user, or the empty string if all went well
     */
    private String loadTasks() {
        try {
            Storage.LoadResult loaded = storage.load();
            tasks = loaded.tasks();
            if (loaded.problems().isEmpty()) {
                return "";
            }
            // The tasks that did load are kept, so the user is told what was lost
            // rather than the whole file being thrown away.
            return ui.getSkippedLinesMessage(loaded.problems(), storage.getFileName());
        } catch (ElsaException e) {
            // A file that cannot be understood is reported once, and the session
            // goes on with an empty list rather than refusing to start.
            tasks = new TaskList();
            return ui.getErrorMessage(e.getMessage());
        }
    }

    /**
     * Starts the chatbot.
     *
     * <p>This starts the chatbot in a terminal. The window starts it through
     * {@link elsa.gui.Launcher} instead, and both share the data file named by
     * {@link #Elsa()}.
     *
     * @param args command line arguments, which the chatbot does not use
     */
    public static void main(String[] args) {
        new Elsa().run();
    }
}
