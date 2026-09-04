package elsa.task;

import java.util.regex.Pattern;

import elsa.Dates;
import elsa.ElsaException;

/**
 * The shape of one line in the data file, and how to read one back.
 *
 * <pre>
 * T | 1 | read book
 * D | 0 | return book | 2019-06-06
 * E | 0 | project meeting | 2019-08-06 | 2019-08-07
 * </pre>
 *
 * <p>The first field is the type letter, the second is the done marker, and the
 * rest are the fields that kind of task carries.
 *
 * <p>This class exists because the format used to be spread over five files: the
 * four task classes each wrote their own line with the separator and markers
 * spelled out again, and the storage class read all of them. Nothing owned the
 * format, so the writing and the reading could drift apart with nothing to catch
 * it. Now the separator, the markers and the type letters are named here once,
 * the task classes write with them, and {@link #decode} reads with them.
 *
 * <p>Storage is left with the file itself: whether it exists, reading its lines
 * and writing them back.
 *
 * <p>The two directions are not symmetrical, and cannot easily be. Writing is
 * done by each task in its own toSaveFormat(), so a task type says how it is
 * written; reading has to look at a type letter before it knows what to build, so
 * it is one switch here. Adding a task type therefore means editing this class as
 * well as writing the new one.
 */
public class TaskFormat {
    /**
     * What separates one field from the next.
     * It is public because a description containing it would split into extra
     * fields and be read back wrongly, so what the user types is checked against
     * it before a task is stored.
     */
    public static final String SEPARATOR = " | ";

    /** The marker written for a task that has been completed. */
    public static final String DONE = "1";

    /** The marker written for a task that has not been completed. */
    public static final String NOT_DONE = "0";

    /** The type letter that begins a todo's line. */
    public static final String TODO = "T";

    /** The type letter that begins a deadline's line. */
    public static final String DEADLINE = "D";

    /** The type letter that begins an event's line. */
    public static final String EVENT = "E";

    /** Every task line has at least a type letter, a done marker and a description. */
    private static final int SHORTEST_LINE = 3;

    /** How many fields a deadline's line has: the three above plus its due date. */
    private static final int DEADLINE_FIELDS = 4;

    /** How many fields an event's line has: the three above plus its two dates. */
    private static final int EVENT_FIELDS = 5;

    /**
     * The separator as something to split on.
     * split() takes a regular expression, in which "|" means "or", so the
     * separator cannot be handed to it as it stands. Pattern.quote wraps it so
     * that every character in it is matched as itself, which keeps the separator
     * defined in one place instead of being spelled out again with escapes.
     */
    private static final String SPLIT_ON = Pattern.quote(SEPARATOR);

    /**
     * Prevents this class from being instantiated. It is a place to keep the
     * format in, not a thing to make one of. Java supplies a public constructor
     * to any class that declares none, so refusing one has to be written down.
     */
    private TaskFormat() {
    }

    /**
     * Turns one line of the data file back into a task.
     * This is the reverse of {@link Task#toSaveFormat()}: the type letter chooses
     * which kind of task to build, and the fields after it fill that task in.
     *
     * @param line one line of the data file
     * @return the task the line describes
     * @throws ElsaException if the line does not follow the format above
     */
    public static Task decode(String line) throws ElsaException {
        String[] fields = line.split(SPLIT_ON);
        if (fields.length < SHORTEST_LINE) {
            throw new ElsaException("it has only " + fields.length
                    + " field(s), and every task needs at least " + SHORTEST_LINE);
        }

        String description = fields[2];
        if (description.isBlank()) {
            throw new ElsaException("its description is blank");
        }

        Task task;
        // Each kind of task needs a different number of fields, so each branch
        // checks it has them before reading the ones beyond the description.
        switch (fields[0]) {
            case TODO -> task = new Todo(description);
            case DEADLINE -> {
                requireFields(fields, DEADLINE_FIELDS);
                task = new Deadline(description, Dates.parse(fields[3]));
            }
            case EVENT -> {
                requireFields(fields, EVENT_FIELDS);
                task = new Event(description, Dates.parse(fields[3]),
                        Dates.parse(fields[4]));
            }
            default -> throw new ElsaException("\"" + fields[0]
                    + "\" is not a task type; it should be " + TODO + ", " + DEADLINE
                    + " or " + EVENT);
        }

        // The second field records whether the task was done when it was saved.
        // Anything other than the two markers means the line cannot be trusted,
        // so it is reported rather than quietly assumed to be not done.
        switch (fields[1]) {
            case DONE -> task.markAsDone();
            case NOT_DONE -> task.markAsNotDone();
            default -> throw new ElsaException("\"" + fields[1]
                    + "\" is not a done marker; it should be " + DONE + " or " + NOT_DONE);
        }
        return task;
    }

    /**
     * Checks that a line carries all the fields its kind of task needs.
     *
     * @param fields   the fields the line was split into
     * @param expected how many fields this kind of task needs
     * @throws ElsaException if the line has fewer fields than expected
     */
    private static void requireFields(String[] fields, int expected) throws ElsaException {
        if (fields.length < expected) {
            throw new ElsaException("a " + fields[0] + " task needs " + expected
                    + " fields, but this line has " + fields.length);
        }
    }
}
