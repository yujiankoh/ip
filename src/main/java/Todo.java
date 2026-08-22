/**
 * A task with no date or time attached to it, for example "borrow book".
 */
public class Todo extends Task {
    /**
     * Creates a todo that is not done yet.
     *
     * @param description the text describing what is to be done
     */
    public Todo(String description) {
        // super(...) calls the Task constructor, which stores the description.
        super(description);
    }

    /**
     * Returns the todo as it should appear to the user,
     * for example "[T][ ] borrow book".
     *
     * @return the todo marker followed by the inherited task text
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
