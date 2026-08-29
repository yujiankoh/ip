package elsa.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link TaskList}, the collection the chatbot keeps its tasks in.
 *
 * <p>Several of its methods pass straight through to the ArrayList inside and are
 * not worth a test of their own. What is tested here is the behaviour that is the
 * list's rather than the ArrayList's: that deleting returns the task removed and
 * closes the gap it left, that marking changes the task in the list rather than a
 * copy of it, and that a list built from tasks read off the disk starts with them
 * already in it.
 */
public class TaskListTest {

    /** A list holding three todos, named so that positions can be told apart. */
    private static TaskList threeTasks() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("first"));
        tasks.add(new Todo("second"));
        tasks.add(new Todo("third"));
        return tasks;
    }

    @Test
    public void newTaskList_noTasks_isEmpty() {
        TaskList tasks = new TaskList();
        assertTrue(tasks.isEmpty());
        assertEquals(0, tasks.size());
    }

    @Test
    public void add_oneTask_growsTheListAndKeepsTheTask() {
        TaskList tasks = new TaskList();
        Todo todo = new Todo("read book");
        tasks.add(todo);
        assertEquals(1, tasks.size());
        assertFalse(tasks.isEmpty());
        assertSame(todo, tasks.get(0));
    }

    /** Tasks are added to the end, so the list stays in the order they were typed. */
    @Test
    public void add_severalTasks_keepsThemInTheOrderAdded() {
        TaskList tasks = threeTasks();
        assertEquals("first", tasks.get(0).description);
        assertEquals("second", tasks.get(1).description);
        assertEquals("third", tasks.get(2).description);
    }

    /**
     * A list built from tasks read off the disk starts with them already in it,
     * which is how a saved list survives a restart.
     */
    @Test
    public void newTaskList_builtFromExistingTasks_holdsThem() {
        ArrayList<Task> loaded = new ArrayList<>();
        loaded.add(new Todo("read book"));
        TaskList tasks = new TaskList(loaded);
        assertEquals(1, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
    }

    // ------------------------------------------------------------------
    // Deleting
    // ------------------------------------------------------------------

    /** The task removed is handed back, so the caller can show which one it was. */
    @Test
    public void delete_middleTask_returnsThatTask() {
        TaskList tasks = threeTasks();
        Task removed = tasks.delete(1);
        assertEquals("second", removed.description);
    }

    @Test
    public void delete_middleTask_closesTheGapItLeft() {
        TaskList tasks = threeTasks();
        tasks.delete(1);
        assertEquals(2, tasks.size());
        assertEquals("first", tasks.get(0).description);
        assertEquals("third", tasks.get(1).description);
    }

    @Test
    public void delete_theOnlyTask_leavesTheListEmpty() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        tasks.delete(0);
        assertTrue(tasks.isEmpty());
    }

    // ------------------------------------------------------------------
    // Marking
    // ------------------------------------------------------------------

    /**
     * Marking has to change the task that is in the list, not a copy of it, or the
     * change would be shown to the user and then not saved.
     */
    @Test
    public void mark_task_marksTheTaskInTheListAndReturnsIt() {
        TaskList tasks = threeTasks();
        Task marked = tasks.mark(1);
        assertEquals("X", marked.getStatusIcon());
        assertEquals("X", tasks.get(1).getStatusIcon());
        assertSame(marked, tasks.get(1));
    }

    @Test
    public void mark_oneTask_leavesTheOthersAlone() {
        TaskList tasks = threeTasks();
        tasks.mark(1);
        assertEquals(" ", tasks.get(0).getStatusIcon());
        assertEquals(" ", tasks.get(2).getStatusIcon());
    }

    @Test
    public void unmark_taskThatWasDone_marksItNotDoneInTheList() {
        TaskList tasks = threeTasks();
        tasks.mark(0);
        Task unmarked = tasks.unmark(0);
        assertEquals(" ", unmarked.getStatusIcon());
        assertEquals(" ", tasks.get(0).getStatusIcon());
    }
}
