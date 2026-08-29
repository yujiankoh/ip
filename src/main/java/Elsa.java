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
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        // Tasks saved by an earlier run are read back here, so the list picks up where
        // the user left off. On the very first run there is no file yet, and load()
        // returns an empty list rather than treating that as a problem.
        TaskList tasks;
        try {
            Storage.LoadResult loaded = Storage.load();
            tasks = loaded.tasks();
            if (!loaded.problems().isEmpty()) {
                // The tasks that did load are kept, so the user is told what was
                // lost rather than the whole file being thrown away.
                ui.showSkippedLines(loaded.problems());
            }
        } catch (ElsaException e) {
            // A file that cannot be understood is reported once, and the session goes
            // on with an empty list rather than refusing to start.
            ui.showError(e.getMessage());
            tasks = new TaskList();
        }

        // The flag ends the loop from inside the switch, where a plain break would only
        // leave the switch. hasNextCommand() guards against input ending without a "bye".
        boolean isRunning = true;
        while (isRunning && ui.hasNextCommand()) {
            Parser.ParsedLine line = Parser.parseLine(ui.readCommand());
            Command command = line.command();
            String arguments = line.arguments();

            try {
                switch (command) {
                case BYE -> {
                    ui.showFarewell();
                    isRunning = false;
                }
                case LIST -> ui.showTasks(tasks);
                case ON -> ui.showTasksOn(tasks, Parser.parseDate(arguments, command));
                case MARK -> {
                    int index = Parser.parseTaskIndex(arguments, tasks.size(), command);
                    Task marked = tasks.mark(index);
                    Storage.save(tasks);
                    ui.showMarked(marked);
                }
                case UNMARK -> {
                    int index = Parser.parseTaskIndex(arguments, tasks.size(), command);
                    Task unmarked = tasks.unmark(index);
                    Storage.save(tasks);
                    ui.showUnmarked(unmarked);
                }
                case DELETE -> {
                    int index = Parser.parseTaskIndex(arguments, tasks.size(), command);
                    // delete() returns the task it took out, so it can be shown to the user.
                    Task removed = tasks.delete(index);
                    Storage.save(tasks);
                    ui.showRemoved(removed, tasks.size());
                }
                case TODO -> addTask(ui, tasks, Parser.parseTodo(arguments));
                case DEADLINE -> addTask(ui, tasks, Parser.parseDeadline(arguments));
                case EVENT -> addTask(ui, tasks, Parser.parseEvent(arguments));
                case NOTHING -> throw new ElsaException("You did not type anything. Try \""
                        + Command.TODO.getUsage() + "\", or \"list\" to see what you have.");
                case UNKNOWN -> throw new ElsaException(
                        "I'm sorry, but I don't know what that means :-(");
                }
            } catch (ElsaException e) {
                // One place to report anything the chatbot could not carry out.
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Adds a task to the list, saves the updated list to the hard disk, and
     * confirms the addition to the user. The three commands that add a task all
     * do these same three things, so they share this method.
     *
     * @param ui    the user interface that confirms the addition
     * @param tasks the task list to add to
     * @param task  the task the user asked to add
     * @throws ElsaException if the updated list could not be saved
     */
    private static void addTask(Ui ui, TaskList tasks, Task task)
            throws ElsaException {
        tasks.add(task);
        // Saved before the confirmation is shown, so the chatbot never claims to
        // have stored a task that did not reach the disk.
        Storage.save(tasks);
        ui.showAdded(task, tasks.size());
    }
}
