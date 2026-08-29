import java.util.ArrayList;

/**
 * The tasks the user is keeping, and the operations that change them.
 *
 * <p>The list itself is private, so nothing outside this class can add to it,
 * reorder it or empty it behind its back. Everything the chatbot does to the
 * list goes through one of the methods here, which means the rules that apply to
 * every change, such as how a task number maps to a position, are stated once.
 *
 * <p>An ArrayList grows as tasks are added, so there is no fixed capacity to
 * track separately: size() is always exactly how many tasks there are.
 */
public class TaskList {
    private final ArrayList<Task> tasks;

    /** Creates an empty task list, as on a first run or after a failed load. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list holding tasks that have already been read from the disk.
     *
     * @param tasks the tasks to start with, in the order they should appear
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Returns how many tasks are in the list.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns whether there are no tasks at all.
     *
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Returns the task at a position, counted from 0.
     *
     * @param index the position of the task
     * @return the task at that position
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Removes the task at a position and returns it, so that the caller can show
     * the user which task was taken out.
     *
     * @param index the position of the task to remove
     * @return the task that was removed
     */
    public Task delete(int index) {
        return tasks.remove(index);
    }

    /**
     * Marks the task at a position as done.
     *
     * @param index the position of the task
     * @return the task that was marked, so the caller can show it
     */
    public Task mark(int index) {
        Task task = tasks.get(index);
        task.markAsDone();
        return task;
    }

    /**
     * Marks the task at a position as not done after all.
     *
     * @param index the position of the task
     * @return the task that was unmarked, so the caller can show it
     */
    public Task unmark(int index) {
        Task task = tasks.get(index);
        task.markAsNotDone();
        return task;
    }
}
