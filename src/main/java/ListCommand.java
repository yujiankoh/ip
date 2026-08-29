/** Shows the whole task list. */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing changes, so the list is not saved. Only commands that alter the
        // list write to the disk.
        ui.showTasks(tasks);
    }
}
