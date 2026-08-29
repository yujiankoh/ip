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

        // The flag ends the loop from inside execute(), where a plain break would
        // only leave its switch. hasNextCommand() guards against input ending
        // without a "bye".
        boolean isRunning = true;
        while (isRunning && ui.hasNextCommand()) {
            Parser.ParsedLine line = Parser.parseLine(ui.readCommand());
            try {
                isRunning = execute(line.command(), line.arguments());
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
     * Carries out one command.
     *
     * @param command   the command the user named
     * @param arguments everything typed after the keyword
     * @return true to carry on reading commands, false once the user has said "bye"
     * @throws ElsaException if the command could not be carried out
     */
    private boolean execute(CommandType command, String arguments) throws ElsaException {
        switch (command) {
        case BYE -> {
            ui.showFarewell();
            return false;
        }
        case LIST -> ui.showTasks(tasks);
        case ON -> ui.showTasksOn(tasks, Parser.parseDate(arguments, command));
        case MARK -> {
            int index = Parser.parseTaskIndex(arguments, tasks.size(), command);
            Task marked = tasks.mark(index);
            storage.save(tasks);
            ui.showMarked(marked);
        }
        case UNMARK -> {
            int index = Parser.parseTaskIndex(arguments, tasks.size(), command);
            Task unmarked = tasks.unmark(index);
            storage.save(tasks);
            ui.showUnmarked(unmarked);
        }
        case DELETE -> {
            int index = Parser.parseTaskIndex(arguments, tasks.size(), command);
            // delete() returns the task it took out, so it can be shown to the user.
            Task removed = tasks.delete(index);
            storage.save(tasks);
            ui.showRemoved(removed, tasks.size());
        }
        case TODO -> addTask(Parser.parseTodo(arguments));
        case DEADLINE -> addTask(Parser.parseDeadline(arguments));
        case EVENT -> addTask(Parser.parseEvent(arguments));
        case NOTHING -> throw new ElsaException("You did not type anything. Try \""
                + CommandType.TODO.getUsage() + "\", or \"list\" to see what you have.");
        case UNKNOWN -> throw new ElsaException(
                "I'm sorry, but I don't know what that means :-(");
        }
        return true;
    }

    /**
     * Adds a task to the list, saves the updated list to the hard disk, and
     * confirms the addition to the user. The three commands that add a task all
     * do these same three things, so they share this method.
     *
     * @param task the task the user asked to add
     * @throws ElsaException if the updated list could not be saved
     */
    private void addTask(Task task) throws ElsaException {
        tasks.add(task);
        // Saved before the confirmation is shown, so the chatbot never claims to
        // have stored a task that did not reach the disk.
        storage.save(tasks);
        ui.showAdded(task, tasks.size());
    }

    public static void main(String[] args) {
        new Elsa(FILE_PATH).run();
    }
}
