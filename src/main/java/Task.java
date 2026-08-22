/**
 * A single task in the user's list, holding its description and whether it is done.
 * Kinds of task that carry extra information, such as {@link Deadline}, extend this class.
 */
public class Task {
    /**
     * Fields are protected rather than private so that the kinds of task
     * that extend this class (todos, deadlines, events) can reuse them.
     */
    protected String description;
    protected boolean isDone;

    /**
     * Creates a task that is not done yet.
     *
     * @param description the text describing what is to be done
     */
    public Task(String description) {
        this.description = description;
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
     * Returns the task as it should appear to the user, for example "[X] read book".
     * Subclasses add their own type marker and extra information around this.
     *
     * @return the status icon in brackets, followed by the description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
