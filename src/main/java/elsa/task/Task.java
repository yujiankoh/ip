package elsa.task;

import java.time.LocalDate;

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
     * Returns whether this task falls on the given date.
     * A plain task carries no date, so the answer is always no. The kinds of task
     * that do carry dates override this and answer for themselves, in the same way
     * they each write themselves differently in toString().
     *
     * @param date the date being asked about
     * @return true if this task falls on that date
     */
    public boolean occursOn(LocalDate date) {
        return false;
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

    /**
     * Returns the task as one line of the data file, for example "1 | read book".
     * Only the parts every task has are written here; each kind of task adds its
     * own type letter and extra fields around this, the same way toString() does.
     *
     * @return whether the task is done, as 1 or 0, followed by its description
     */
    public String toSaveFormat() {
        // The markers and the separator are named in TaskFormat, which also reads
        // them back, so the two directions cannot drift apart.
        return (isDone ? TaskFormat.DONE : TaskFormat.NOT_DONE)
                + TaskFormat.SEPARATOR + description;
    }
}
