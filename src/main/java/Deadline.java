import java.time.LocalDate;

/**
 * A task that has to be done before a stated date,
 * for example "return book (by: Oct 15 2019)".
 */
public class Deadline extends Task {
    /**
     * The date the task is due. A LocalDate rather than text, so that the date
     * is known to be a real one and could later be compared with another.
     */
    protected LocalDate by;

    /**
     * Creates a deadline that is not done yet.
     *
     * @param description the text describing what is to be done
     * @param by          the date it has to be done by
     */
    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline as it should appear to the user,
     * for example "[D][ ] return book (by: Oct 15 2019)".
     *
     * @return the deadline marker, the inherited task text, and the due time
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + Dates.format(by) + ")";
    }

    /**
     * Returns the deadline as one line of the data file,
     * for example "D | 0 | return book | 2019-10-15".
     *
     * @return the deadline's type letter, the inherited fields, and the due time
     */
    @Override
    public String toSaveFormat() {
        return "D | " + super.toSaveFormat() + " | " + Dates.toSaveFormat(by);
    }
}
