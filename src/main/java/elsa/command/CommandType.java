package elsa.command;

import java.util.ArrayList;
import java.util.List;

/**
 * The kinds of command the chatbot understands. Each constant pairs the keyword the
 * user types with the usage shown when that command is typed wrongly, so the two
 * cannot be mismatched.
 *
 * <p>This is the vocabulary of the language, not the behaviour: it says which words
 * are commands and how each is written, and nothing about what any of them does.
 *
 * <p>The order the constants are declared in is the order {@link #getUsages}
 * hands them back, which is the order the window lists them in when it opens. It
 * therefore runs from what a new user needs first to what they need last:
 * putting a task in, looking at what is there, changing it, asking what else
 * there is, and leaving. A new command goes where a user would want to meet it,
 * not on the end.
 */
public enum CommandType {
    /** Adds a task with no date attached to it. */
    TODO("todo", "todo <description>"),

    /** Adds a task that has to be done before a stated date. */
    DEADLINE("deadline", "deadline <description> /by <date>"),

    /** Adds a task that runs between two stated dates. */
    EVENT("event", "event <description> /from <date> /to <date>"),

    /** Shows every task in the list. */
    LIST("list", "list"),

    /** Shows the tasks falling on one date. */
    ON("on", "on <date>"),

    /** Shows the tasks whose description contains a keyword. */
    FIND("find", "find <keyword>"),

    /** Marks a task as done. */
    MARK("mark", "mark <task number>"),

    /** Marks a task as not done after all. */
    UNMARK("unmark", "unmark <task number>"),

    /** Removes a task from the list. */
    DELETE("delete", "delete <task number>"),

    /** Lists how every command is written. */
    HELP("help", "help"),

    /** Ends the session. */
    BYE("bye", "bye"),

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
     * Returns how every command a user can type is written, in the order a new
     * user should meet them.
     *
     * <p>NOTHING and UNKNOWN are left out. They are not commands anyone types:
     * they are what the chatbot calls a blank line and a word it does not know,
     * and neither has a way of being written down.
     *
     * <p>This is read by whatever introduces the chatbot to the user, so that
     * the introduction cannot fall out of step with what the chatbot actually
     * understands. A command added above appears in it without anything else
     * being edited.
     *
     * @return one usage line per command, for example "find &lt;keyword&gt;"
     */
    public static List<String> getUsages() {
        List<String> usages = new ArrayList<>();
        for (CommandType command : values()) {
            if (command.usage != null && !command.usage.isEmpty()) {
                usages.add(command.usage);
            }
        }
        return usages;
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
