package elsa.parser;

import elsa.Dates;
import elsa.ElsaException;
import elsa.command.AddCommand;
import elsa.command.Command;
import elsa.command.CommandType;
import elsa.command.DeleteCommand;
import elsa.command.ExitCommand;
import elsa.command.ListCommand;
import elsa.command.MarkCommand;
import elsa.command.OnCommand;
import elsa.command.TaskNumberCommand;
import elsa.command.UnmarkCommand;
import elsa.task.Deadline;
import elsa.task.Event;
import elsa.task.TaskFormat;
import elsa.task.Todo;
import elsa.ui.Ui;
import java.time.LocalDate;

/**
 * Makes sense of what the user typed.
 *
 * <p>This class turns a typed line into a {@link Command} ready to be carried
 * out: it works out which command was named, reads the arguments that command
 * needs, and hands back an object holding them. Everything it rejects, it rejects
 * with an explanation written for the user, so the caller can hand that straight
 * to {@link Ui} without knowing why the input was wrong.
 *
 * <p>Nothing here changes the task list or touches the disk. Reading a command
 * and carrying it out are separate jobs, and keeping them apart means the wording
 * of an error and the effect of a command can be changed independently.
 */
public class Parser {
    /** Separates a deadline's description from the date it is due. */
    private static final String BY_SEPARATOR = "/by";

    /** Separates an event's description from its start date. */
    private static final String FROM_SEPARATOR = "/from";

    /** Separates an event's start date from its end date. */
    private static final String TO_SEPARATOR = "/to";

    /**
     * One line of input, split into the command it names and the rest of the line.
     *
     * @param command   the command the first word names, or UNKNOWN if none does
     * @param arguments everything after the first word, trimmed; empty if there is none
     */
    public record ParsedLine(CommandType command, String arguments) {
    }

    /**
     * Prevents this class from being instantiated. Reading a line needs nothing
     * remembered between calls, so every method here is static and a Parser
     * object would hold nothing. Java supplies a public constructor to any class
     * that declares none, so refusing one has to be written down.
     */
    private Parser() {
    }

    /**
     * Reads a typed line as a command ready to be carried out.
     *
     * <p>The switch here chooses which kind of command to build, which is the one
     * place text still has to be turned into a type. What it no longer decides is
     * what any of them does: that lives in the command classes, so this switch
     * gains a line when a command is added and never changes otherwise.
     *
     * @param fullCommand the whole line the user typed, already trimmed
     * @return the command the line asks for
     * @throws ElsaException if the line names no command, or its arguments are wrong
     */
    public static Command parse(String fullCommand) throws ElsaException {
        ParsedLine line = parseLine(fullCommand);
        CommandType type = line.command();
        String arguments = line.arguments();

        return switch (type) {
        case BYE -> new ExitCommand();
        case LIST -> new ListCommand();
        case ON -> new OnCommand(parseDate(arguments, type));
        case MARK -> new MarkCommand(parseTaskNumber(arguments, type));
        case UNMARK -> new UnmarkCommand(parseTaskNumber(arguments, type));
        case DELETE -> new DeleteCommand(parseTaskNumber(arguments, type));
        case TODO -> new AddCommand(parseTodo(arguments));
        case DEADLINE -> new AddCommand(parseDeadline(arguments));
        case EVENT -> new AddCommand(parseEvent(arguments));
        case NOTHING -> throw new ElsaException("You did not type anything. Try \""
                + CommandType.TODO.getUsage() + "\", or \"list\" to see what you have.");
        case UNKNOWN -> throw new ElsaException(
                "I'm sorry, but I don't know what that means :-(");
        };
    }

    /**
     * Splits a typed line into the command it names and the arguments that follow.
     *
     * @param line the line the user typed, already trimmed
     * @return the command and its arguments
     */
    private static ParsedLine parseLine(String line) {
        // Every line is one keyword plus whatever follows it. The split is on a run
        // of whitespace so that a tab, or several spaces, separates them just as one
        // space does. Splitting here means "todo" with nothing after it is recognised
        // as a todo missing its description, rather than as an unknown command.
        String[] words = line.split("\\s+", 2);
        String arguments = (words.length > 1) ? words[1].trim() : "";
        return new ParsedLine(CommandType.fromKeyword(words[0]), arguments);
    }

    /**
     * Reads the arguments of a "todo" command as a task.
     *
     * @param arguments everything the user typed after the keyword
     * @return the todo the user described
     * @throws ElsaException if there is no description, or it cannot be stored
     */
    private static Todo parseTodo(String arguments) throws ElsaException {
        CommandType command = CommandType.TODO;
        requireDescription(arguments, command);
        requireNoSeparator(arguments, "description of a todo", command);
        return new Todo(arguments);
    }

    /**
     * Reads the arguments of a "deadline" command as a task.
     *
     * @param arguments everything the user typed after the keyword
     * @return the deadline the user described
     * @throws ElsaException if a part is missing, empty, unstorable or not a date
     */
    private static Deadline parseDeadline(String arguments) throws ElsaException {
        CommandType command = CommandType.DEADLINE;
        requireDescription(arguments, command);
        // Limit of 2 keeps any later "/by" as part of the due date itself.
        String[] parts = requireSeparator(arguments, BY_SEPARATOR, command);
        String description = requireNonEmpty(parts[0], "description of a deadline", command);
        LocalDate by = requireDate(requireNonEmpty(parts[1],
                "due date after " + BY_SEPARATOR, command), command);
        return new Deadline(description, by);
    }

    /**
     * Reads the arguments of an "event" command as a task.
     *
     * @param arguments everything the user typed after the keyword
     * @return the event the user described
     * @throws ElsaException if a part is missing, empty, unstorable or not a date
     */
    private static Event parseEvent(String arguments) throws ElsaException {
        CommandType command = CommandType.EVENT;
        requireDescription(arguments, command);
        // Split off the description first, then split what remains into the two dates.
        String[] parts = requireSeparator(arguments, FROM_SEPARATOR, command);
        String description = requireNonEmpty(parts[0], "description of an event", command);
        String[] dates = requireSeparator(parts[1], TO_SEPARATOR, command);
        LocalDate from = requireDate(requireNonEmpty(dates[0],
                "start date after " + FROM_SEPARATOR, command), command);
        LocalDate to = requireDate(requireNonEmpty(dates[1],
                "end date after " + TO_SEPARATOR, command), command);
        return new Event(description, from, to);
    }

    /**
     * Reads the argument of a command that asks about one date, such as "on".
     *
     * @param arguments everything the user typed after the keyword
     * @param command   the command being run, which supplies the usage to show
     * @return the date the user named
     * @throws ElsaException if no date was given, or it is not a date
     */
    private static LocalDate parseDate(String arguments, CommandType command)
            throws ElsaException {
        if (arguments.isEmpty()) {
            throw new ElsaException("Which date? Use: " + command.getUsage()
                    + ", for example: on 2019-10-15.");
        }
        return requireDate(arguments, command);
    }

    /**
     * Reads the task number the user typed, as they typed it, counting from 1.
     *
     * <p>Only the writing is checked here: that a number was given at all, and that
     * it is a whole one. Whether a task with that number exists depends on how many
     * tasks there are, which this class has no way of knowing, so that check belongs
     * to the command instead; see {@link TaskNumberCommand}.
     *
     * @param arguments everything the user typed after the command keyword
     * @param command   the command being run, used to word the error messages
     * @return the number the user typed, counting from 1
     * @throws ElsaException if no number was given, or it is not a whole number
     */
    private static int parseTaskNumber(String arguments, CommandType command)
            throws ElsaException {
        String keyword = command.getKeyword();
        if (arguments.isEmpty()) {
            throw new ElsaException("Which task? Use: " + keyword
                    + " <task number>, for example: " + keyword + " 2.");
        }

        try {
            return Integer.parseInt(arguments);
        } catch (NumberFormatException e) {
            // Rethrown as an ElsaException so it reaches the user instead of ending the session.
            throw new ElsaException("\"" + arguments + "\" is not a task number. Use a whole"
                    + " number, for example: " + keyword + " 2.");
        }
    }

    /**
     * Checks that a command that adds a task was given something to add.
     *
     * @param arguments everything the user typed after the command keyword
     * @param command   the command being run, which supplies its own name and usage
     * @throws ElsaException if nothing was typed after the keyword
     */
    private static void requireDescription(String arguments, CommandType command)
            throws ElsaException {
        if (arguments.isEmpty()) {
            String keyword = command.getKeyword();
            // "an event" but "a todo": pick the article that reads correctly.
            String article = ("aeiou".indexOf(keyword.charAt(0)) >= 0) ? "an" : "a";
            throw new ElsaException("The description of " + article + " " + keyword
                    + " cannot be empty. Use: " + command.getUsage());
        }
    }

    /**
     * Splits text on a separator the command requires, reporting its absence to the user.
     *
     * @param text      the text to split
     * @param separator the separator the command cannot do without, such as "/by"
     * @param command   the command being run, which supplies the usage to show
     * @return the two pieces on either side of the first occurrence of the separator
     * @throws ElsaException if the separator does not appear in the text
     */
    private static String[] requireSeparator(String text, String separator, CommandType command)
            throws ElsaException {
        // Limit of 2 keeps any later occurrence as part of the second piece.
        String[] parts = text.split(separator, 2);
        if (parts.length < 2) {
            throw new ElsaException("I could not find \"" + separator + "\" in that. Use: "
                    + command.getUsage());
        }
        return parts;
    }

    /**
     * Reads a piece of a command as a date, saying how to write one if it is not.
     * The date itself is understood by {@link Dates}; this method only adds the
     * usage of the command being run, so the user can see the whole line again.
     *
     * @param value   the text the user gave as a date
     * @param command the command being run, which supplies the usage to show
     * @return the date that text describes
     * @throws ElsaException if the text is not a date
     */
    private static LocalDate requireDate(String value, CommandType command)
            throws ElsaException {
        try {
            return Dates.parse(value);
        } catch (ElsaException e) {
            throw new ElsaException(e.getMessage() + ". Use: " + command.getUsage());
        }
    }

    /**
     * Checks that a piece of a command does not contain the text that separates
     * one field from the next in the data file. A description holding that text
     * would be split into extra fields when the file is read back, so the task
     * would return changed, or not at all. Refusing it now is clearer to the
     * user than losing part of their task later.
     *
     * @param value   the piece to check
     * @param what    what the piece is, named for the error message
     * @param command the command being run, which supplies the usage to show
     * @throws ElsaException if the piece contains the separator
     */
    private static void requireNoSeparator(String value, String what, CommandType command)
            throws ElsaException {
        if (value.contains(TaskFormat.SEPARATOR)) {
            throw new ElsaException("The " + what + " cannot contain \""
                    + TaskFormat.SEPARATOR.trim() + "\" with a space on each side, because"
                    + " that is how the parts of a stored task are separated. Use: "
                    + command.getUsage());
        }
    }

    /**
     * Checks that a piece of a command was actually filled in.
     *
     * @param value   the piece to check, before trimming
     * @param what    what the piece is, named for the error message
     * @param command the command being run, which supplies the usage to show
     * @return the value with surrounding spaces removed
     * @throws ElsaException if the piece is empty once trimmed
     */
    private static String requireNonEmpty(String value, String what, CommandType command)
            throws ElsaException {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ElsaException("The " + what + " cannot be empty. Use: "
                    + command.getUsage());
        }
        requireNoSeparator(trimmed, what, command);
        return trimmed;
    }
}
