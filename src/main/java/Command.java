/**
 * Something the user has asked the chatbot to do, ready to be carried out.
 *
 * <p>A command is built by {@link Parser} from the line the user typed, and knows
 * everything it needs before it runs: which task to add, which number to delete,
 * which date to ask about. Running it is then a matter of calling
 * {@link #execute}, without anyone having to ask which kind of command it is.
 *
 * <p>This is why the class exists. Before it, one switch in the chatbot listed
 * what every command did, so adding a command meant editing that switch, and the
 * behaviour of nine unrelated commands sat in one method. Now each command is a
 * class of its own, and a new one is a new file that nothing else has to know
 * about.
 */
public abstract class Command {
    /**
     * Carries this command out.
     *
     * <p>The three things a command might need are handed in rather than held as
     * fields, so that a command can be built by the parser, which has none of
     * them, and run later by the chatbot, which has all three.
     *
     * @param tasks   the task list to read or change
     * @param ui      the user interface to report through
     * @param storage the store to write the list to if it changed
     * @throws ElsaException if the command cannot be carried out
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws ElsaException;

    /**
     * Returns whether the session should end after this command.
     * Only leaving says yes, so that is the answer here and {@link ExitCommand}
     * overrides it. A new command therefore ends the session only if it says so.
     *
     * @return true if the chatbot should stop reading commands
     */
    public boolean isExit() {
        return false;
    }
}
