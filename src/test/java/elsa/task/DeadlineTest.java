package elsa.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Deadline}, the kind of task that has to be done by a stated date.
 *
 * <p>Two dates are used throughout: one comfortably in the past and one
 * comfortably in the future. A deadline is overdue or not depending on today's
 * date, so a date near today would make these tests start failing on their own
 * one day. 2019 stays past and 2999 stays future for as long as anyone will run
 * them. The text UI tests use the same two years for the same reason.
 */
public class DeadlineTest {

    private static final LocalDate PAST = LocalDate.of(2019, 10, 15);
    private static final LocalDate FUTURE = LocalDate.of(2999, 1, 1);

    // ------------------------------------------------------------------
    // Overdue
    // ------------------------------------------------------------------

    @Test
    public void isOverdue_pastDateNotDone_returnsTrue() {
        assertTrue(new Deadline("return book", PAST).isOverdue());
    }

    @Test
    public void isOverdue_futureDateNotDone_returnsFalse() {
        assertFalse(new Deadline("return book", FUTURE).isOverdue());
    }

    /**
     * The rule is not "the date has passed" but "the date has passed and the task
     * is still owed". A deadline that has been done is not overdue however old it
     * is, which is the half of the rule that is easy to drop.
     */
    @Test
    public void isOverdue_pastDateAlreadyDone_returnsFalse() {
        Deadline deadline = new Deadline("return book", PAST);
        deadline.markAsDone();
        assertFalse(deadline.isOverdue());
    }

    @Test
    public void isOverdue_pastDateDoneThenUndone_returnsTrueAgain() {
        Deadline deadline = new Deadline("return book", PAST);
        deadline.markAsDone();
        deadline.markAsNotDone();
        assertTrue(deadline.isOverdue());
    }

    /**
     * A deadline due today is still owed, not late: the check is that the date is
     * strictly before today. Written with the clock rather than a fixed date,
     * because "today" is the one value that cannot be stated in advance.
     */
    @Test
    public void isOverdue_dueToday_returnsFalse() {
        assertFalse(new Deadline("return book", LocalDate.now()).isOverdue());
    }

    @Test
    public void isOverdue_dueYesterday_returnsTrue() {
        assertTrue(new Deadline("return book", LocalDate.now().minusDays(1)).isOverdue());
    }

    // ------------------------------------------------------------------
    // Falling on a date
    // ------------------------------------------------------------------

    @Test
    public void occursOn_theDueDate_returnsTrue() {
        assertTrue(new Deadline("return book", PAST).occursOn(PAST));
    }

    @Test
    public void occursOn_theDayBeforeOrAfter_returnsFalse() {
        Deadline deadline = new Deadline("return book", PAST);
        assertFalse(deadline.occursOn(PAST.minusDays(1)));
        assertFalse(deadline.occursOn(PAST.plusDays(1)));
    }

    // ------------------------------------------------------------------
    // Writing it out
    // ------------------------------------------------------------------

    @Test
    public void toString_futureDeadline_showsTypeStatusAndDueDate() {
        assertEquals("[D][ ] return book (by: Jan 01 2999)",
                new Deadline("return book", FUTURE).toString());
    }

    @Test
    public void toString_overdueDeadline_addsTheOverdueNote() {
        assertEquals("[D][ ] return book (by: Oct 15 2019) -- overdue",
                new Deadline("return book", PAST).toString());
    }

    /** Marking an old deadline done should remove the note, not just tick the box. */
    @Test
    public void toString_pastDeadlineMarkedDone_dropsTheOverdueNote() {
        Deadline deadline = new Deadline("return book", PAST);
        deadline.markAsDone();
        assertEquals("[D][X] return book (by: Oct 15 2019)", deadline.toString());
    }

    /**
     * The date is shown in the display form and stored in the ISO form. Showing
     * the stored form, or storing the shown one, would both look almost right.
     */
    @Test
    public void toSaveFormat_notDone_writesTheDateInTheStoredForm() {
        assertEquals("D | 0 | return book | 2019-10-15",
                new Deadline("return book", PAST).toSaveFormat());
    }

    @Test
    public void toSaveFormat_done_writesTheDoneMarker() {
        Deadline deadline = new Deadline("return book", PAST);
        deadline.markAsDone();
        assertEquals("D | 1 | return book | 2019-10-15", deadline.toSaveFormat());
    }

    /** The overdue note is for the user only and must never reach the data file. */
    @Test
    public void toSaveFormat_overdueDeadline_doesNotWriteTheOverdueNote() {
        assertFalse(new Deadline("return book", PAST).toSaveFormat().contains("overdue"));
    }

    @Test
    public void toSaveFormatThenDecode_deadline_givesAnEqualDeadline() throws Exception {
        Deadline deadline = new Deadline("return book", PAST);
        deadline.markAsDone();
        Task reloaded = TaskFormat.decode(deadline.toSaveFormat());
        assertEquals(deadline.toString(), reloaded.toString());
    }
}
