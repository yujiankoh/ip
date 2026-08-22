/**
 * The commands the chatbot understands. Each constant pairs the keyword the user types
 * with the usage shown when that command is typed wrongly, so the two cannot be mismatched.
 */
public enum Command {
    BYE("bye", "bye"),
    LIST("list", "list"),
    MARK("mark", "mark <task number>"),
    UNMARK("unmark", "unmark <task number>"),
    DELETE("delete", "delete <task number>"),
    TODO("todo", "todo <description>"),
    DEADLINE("deadline", "deadline <description> /by <when>"),
    EVENT("event", "event <description> /from <start> /to <end>"),

    /** The user pressed enter without typing anything, so the keyword is the empty string. */
    NOTHING("", ""),

    /**
     * The keyword matched no command. Its keyword is null rather than a real word,
     * so that no input can ever match it by accident in fromKeyword.
     */
    UNKNOWN(null, null);

    private final String keyword;
    private final String usage;

    /**
     * Enum constructors are always private: the constants listed above are the only
     * instances that will ever exist.
     *
     * @param keyword the word the user types to invoke this command
     * @param usage   the correct way to type this command, shown in error messages
     */
    Command(String keyword, String usage) {
        this.keyword = keyword;
        this.usage = usage;
    }

    /**
     * Returns the word the user types to invoke this command.
     *
     * @return the keyword, for example "deadline"
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Returns the correct way to type this command.
     *
     * @return the usage text, for example "deadline &lt;description&gt; /by &lt;when&gt;"
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Finds the command a keyword refers to.
     *
     * @param keyword the first word of the line the user typed
     * @return the matching command, or UNKNOWN if no command uses that keyword
     */
    public static Command fromKeyword(String keyword) {
        // values() returns every constant declared above, in declaration order.
        for (Command command : values()) {
            if (keyword.equals(command.keyword)) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
