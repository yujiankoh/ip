/**
 * A single task in the user's list, holding its description and whether it is done.
 */
public class Task {
    /**
     * Fields are protected rather than private so that future kinds of task
     * (todos, deadlines, events) can extend this class and reuse them.
     */
    protected String description;
    protected boolean isDone;

    /**
     * Marker shown before the status icon to say what kind of task this is,
     * for example "[T]" for a todo. Empty for a task of no particular kind.
     */
    protected String typeIcon;

    /**
     * Extra information shown after the description, for example "(by: Sunday)".
     * Empty for a task that carries no date or time.
     */
    protected String detail;

    /**
     * Creates a task of no particular kind that is not done yet.
     *
     * @param description the text describing what is to be done
     */
    public Task(String description) {
        // Delegates to the constructor below rather than repeating its work.
        this(description, "");
    }

    /**
     * Creates a task of the given kind, with no extra detail, that is not done yet.
     *
     * @param description the text describing what is to be done
     * @param typeIcon    marker for the kind of task, for example "[T]"
     */
    public Task(String description, String typeIcon) {
        this(description, typeIcon, "");
    }

    /**
     * Creates a task of the given kind that is not done yet.
     *
     * @param description the text describing what is to be done
     * @param typeIcon    marker for the kind of task, for example "[D]"
     * @param detail      extra information to show after the description,
     *                    for example "(by: Sunday)"
     */
    public Task(String description, String typeIcon, String detail) {
        this.description = description;
        this.typeIcon = typeIcon;
        this.detail = detail;
        this.isDone = false;
    }

    /**
     * Returns the single character shown inside the status brackets.
     *
     * @return "X" when the task is done, a space otherwise
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " "); // mark done task with X
    }

    /** Records this task as completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Records this task as not completed after all. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns the task as it should appear to the user,
     * for example "[D][ ] return book (by: Sunday)".
     *
     * @return the type marker, the status icon in brackets, the description,
     *         and any extra detail
     */
    @Override
    public String toString() {
        String suffix = detail.isEmpty() ? "" : " " + detail;
        return typeIcon + "[" + getStatusIcon() + "] " + description + suffix;
    }
}
