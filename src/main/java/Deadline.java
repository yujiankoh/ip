/**
 * A task that has to be done before a stated point in time,
 * for example "return book (by: Sunday)".
 */
public class Deadline extends Task {
    /** When the task is due. Kept as free text; no real date is parsed. */
    protected String by;

    /**
     * Creates a deadline that is not done yet.
     *
     * @param description the text describing what is to be done
     * @param by          when it has to be done by
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline as it should appear to the user,
     * for example "[D][ ] return book (by: Sunday)".
     *
     * @return the deadline marker, the inherited task text, and the due time
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
