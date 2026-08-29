package elsa.command;

/**
 * The kinds of command the chatbot understands. Each constant pairs the keyword the
 * user types with the usage shown when that command is typed wrongly, so the two
 * cannot be mismatched.
 *
 * <p>This is the vocabulary of the language, not the behaviour: it says which words
 * are commands and how each is written, and nothing about what any of them does.
 */
public enum CommandType {
    BYE("bye", "bye"),
    LIST("list", "list"),
    ON("on", "on <date>"),
    MARK("mark", "mark <task number>"),
    UNMARK("unmark", "unmark <task number>"),
    DELETE("delete", "delete <task number>"),
    TODO("todo", "todo <description>"),
    DEADLINE("deadline", "deadline <description> /by <date>"),
    EVENT("event", "event <description> /from <date> /to <date>"),

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
    CommandType(String keyword, String usage) {
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
    public static CommandType fromKeyword(String keyword) {
        // values() returns every constant declared above, in declaration order.
        for (CommandType command : values()) {
            if (keyword.equals(command.keyword)) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
