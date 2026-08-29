/** Removes one task from the list. */
public class DeleteCommand extends TaskNumberCommand {
    /**
     * Creates a command that will remove the numbered task.
     *
     * @param number the task number the user typed, counting from 1
     */
    public DeleteCommand(int number) {
        super(number, CommandType.DELETE);
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws ElsaException {
        // delete() returns the task it took out, so it can be shown to the user.
        Task removed = tasks.delete(indexIn(tasks));
        storage.save(tasks);
        ui.showRemoved(removed, tasks.size());
    }
}
