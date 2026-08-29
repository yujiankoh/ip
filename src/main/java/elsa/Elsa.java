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
     * Runs one session: greets the user, reads the saved tasks, then carries out
     * commands until the user says "bye" or the input ends.
     */
    public void run() {
        ui.showWelcome();
        loadTasks();

        // Each command says whether the session should end, so the loop does not
        // need to know which one means goodbye. hasNextCommand() guards against
        // input ending without a "bye".
        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            try {
                Command command = Parser.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (ElsaException e) {
                // One place to report anything the chatbot could not carry out.
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Reads the tasks saved by an earlier run into the list.
     * On the very first run there is no file yet, and load() returns an empty
     * list rather than treating that as a problem.
     */
    private void loadTasks() {
        try {
            Storage.LoadResult loaded = storage.load();
            tasks = loaded.tasks();
            if (!loaded.problems().isEmpty()) {
                // The tasks that did load are kept, so the user is told what was
                // lost rather than the whole file being thrown away.
                ui.showSkippedLines(loaded.problems(), storage.getFileName());
            }
        } catch (ElsaException e) {
            // A file that cannot be understood is reported once, and the session
            // goes on with an empty list rather than refusing to start.
            ui.showError(e.getMessage());
            tasks = new TaskList();
        }
    }

    /**
     * Starts the chatbot.
     *
     * <p>The data file is named here rather than inside {@link Storage}, so that
     * the one place deciding where the tasks live is the program's starting point.
     *
     * @param args command line arguments, which the chatbot does not use
     */
    public static void main(String[] args) {
        new Elsa(FILE_PATH).run();
    }
}
