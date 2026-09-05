package elsa.command;

import elsa.storage.Storage;
import elsa.task.TaskList;
import elsa.ui.Ui;

/** Shows the tasks whose description contains a keyword. */
public class FindCommand extends Command {
    private final String keyword;

    /**
     * Creates a command that will show the tasks matching the given keyword.
     *
     * @param keyword the text the user is looking for
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing changes, so the list is not saved.
        return ui.getMatchingTasksMessage(tasks, keyword);
    }
}
