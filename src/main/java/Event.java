/**
 * A task that runs between two stated points in time,
 * for example "project meeting (from: Mon 2pm to: 4pm)".
 */
public class Event extends Task {
    /** When the event starts. Kept as free text; no real date is parsed. */
    protected String from;

    /** When the event ends. Kept as free text; no real date is parsed. */
    protected String to;

    /**
     * Creates an event that is not done yet.
     *
     * @param description the text describing what happens
     * @param from        when it starts
     * @param to          when it ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event as it should appear to the user,
     * for example "[E][ ] project meeting (from: Mon 2pm to: 4pm)".
     *
     * @return the event marker, the inherited task text, and both times
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }

    /**
     * Returns the event as one line of the data file,
     * for example "E | 0 | project meeting | Mon 2pm | 4pm".
     * The two times are kept in separate fields rather than as one piece of text,
     * so that reading the file back later does not have to split them apart again.
     *
     * @return the event's type letter, the inherited fields, and both times
     */
    @Override
    public String toSaveFormat() {
        return "E | " + super.toSaveFormat() + " | " + from + " | " + to;
    }
}
