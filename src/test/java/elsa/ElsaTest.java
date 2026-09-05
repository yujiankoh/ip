package elsa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import elsa.command.CommandType;

/**
 * Tests {@link Elsa#getResponse} and {@link Elsa#startSession}, the pair of
 * methods the window speaks to the chatbot through.
 *
 * <p>These two are worth testing because of how much stands behind them. One
 * call reads the line, works out which command it names, carries that command
 * out against the task list, saves the list, and words the answer. The terminal
 * reaches all of that through a loop the text UI tests already drive end to end,
 * but the window reaches it only here, so a mistake on this path would show up
 * in no other suite.
 *
 * <p>Each test builds its own chatbot on a file inside the folder JUnit provides
 * through {@code @TempDir}, never through {@link Elsa#Elsa()}, which would read
 * and overwrite the tasks of whoever is running the tests.
 *
 * <p>The expected wording is written out in full rather than compared against
 * whatever {@link elsa.ui.Ui} currently produces. Wording is what the user
 * reads, so it is part of what these methods promise, and a test that asked the
 * program what it says could never disagree with it.
 */
public class ElsaTest {

    /** The chatbot's opening words, without the banner the terminal adds. */
    private static final String GREETING = "Hello! I'm Elsa.\nDo you want to build a snowman?";

    /** What the chatbot says as the session ends. */
    private static final String FAREWELL = "The cold never bother me anyways!";

    /** The prefix put in front of anything the chatbot could not do. */
    private static final String ERROR_PREFIX = "OLAF!!! ";

    /** Builds a chatbot whose tasks live in a file inside the folder given. */
    private static Elsa elsaIn(Path folder) {
        return new Elsa(folder.resolve("tasks.txt").toString());
    }

    /** Builds a chatbot that starts from the saved lines given. */
    private static Elsa elsaIn(Path folder, String... savedLines) throws IOException {
        Path file = folder.resolve("tasks.txt");
        Files.write(file, List.of(savedLines));
        return new Elsa(file.toString());
    }

    // ------------------------------------------------------------------
    // Opening a session
    // ------------------------------------------------------------------

    @Test
    public void startSession_noSavedFile_greetsWithoutComplaining(@TempDir Path folder) {
        String opening = elsaIn(folder).startSession();

        assertTrue(opening.startsWith(GREETING), "the greeting comes first");
        assertFalse(opening.contains(ERROR_PREFIX), "there is nothing to complain about");
    }

    /**
     * The window has no menu, so the opening message is the only place a
     * first-time user is pointed anywhere. It has to name the one command that
     * leads to all the others, or nothing does.
     */
    @Test
    public void startSession_always_pointsTheUserAtHelp(@TempDir Path folder) {
        String opening = elsaIn(folder).startSession();

        assertTrue(opening.contains(CommandType.HELP.getKeyword()),
                "the opening message never mentions help");
    }

    /**
     * Help is what the greeting sends the user to, so every command has to
     * appear in it. One missing is unreachable in the window for anyone who does
     * not already know it.
     */
    @Test
    public void getResponse_help_listsEveryCommandTheChatbotUnderstands(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        String help = elsa.getResponse("help");
        for (String usage : CommandType.getUsages()) {
            assertTrue(help.contains(usage), "help never mentions: " + usage);
        }
    }

    /**
     * Four of the commands help lists take a date, so help is also the only
     * place the window says what a date may look like. It is taken from the same
     * constant that refuses an unreadable date, so a form accepted by one and
     * not named by the other would be a contradiction.
     */
    @Test
    public void getResponse_help_saysHowToWriteADate(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        assertTrue(elsa.getResponse("help").contains(Dates.ACCEPTED_FORMS),
                "help never says how to write a date");
    }

    /** Help only reads; it must not end the session or disturb the task list. */
    @Test
    public void getResponse_help_changesNothing(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");

        elsa.getResponse("help");

        assertFalse(elsa.isExiting(), "help does not end the session");
        assertEquals("Here are the tasks in your list:\n1.[T][ ] read book",
                elsa.getResponse("list"), "help leaves the task list alone");
    }

    @Test
    public void startSession_savedTasks_greetsAndReadsThemBack(@TempDir Path folder) throws IOException {
        Elsa elsa = elsaIn(folder, "T | 1 | read book", "T | 0 | buy milk");
        assertTrue(elsa.startSession().startsWith(GREETING), "the greeting comes first");

        assertEquals("Here are the tasks in your list:\n1.[T][X] read book\n2.[T][ ] buy milk",
                elsa.getResponse("list"));
    }

    @Test
    public void startSession_lineThatCannotBeRead_keepsTheRestAndSaysSo(@TempDir Path folder) throws IOException {
        Elsa elsa = elsaIn(folder, "T | 1 | read book", "nonsense");
        String opening = elsa.startSession();

        assertTrue(opening.startsWith(GREETING), "the greeting still comes first");
        assertTrue(opening.contains(ERROR_PREFIX), "the complaint is marked as one");
        assertTrue(opening.contains("1 line"), "the user is told how much was lost");
        assertEquals("Here are the tasks in your list:\n1.[T][X] read book",
                elsa.getResponse("list"), "the line that did load is kept");
    }

    // ------------------------------------------------------------------
    // Carrying out commands
    // ------------------------------------------------------------------

    @Test
    public void getResponse_todo_confirmsAndCountsTheList(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        assertEquals("Got it. I've added this task:\n  [T][ ] read book\n"
                + "Now you have 1 task in the list.", elsa.getResponse("todo read book"));
        assertEquals("Got it. I've added this task:\n  [T][ ] buy milk\n"
                + "Now you have 2 tasks in the list.", elsa.getResponse("todo buy milk"));
    }

    @Test
    public void getResponse_listWithNothingInIt_saysSoRatherThanShowingAnEmptyList(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        assertEquals("Into the Unknown.", elsa.getResponse("list"));
    }

    @Test
    public void getResponse_mark_confirmsAndChangesTheTask(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");

        assertEquals("Nice! I've marked this task as done:\n  [T][X] read book",
                elsa.getResponse("mark 1"));
        assertEquals("OK, I've marked this task as not done yet:\n  [T][ ] read book",
                elsa.getResponse("unmark 1"));
    }

    @Test
    public void getResponse_delete_confirmsAndShortensTheList(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");

        assertEquals("Noted. I've removed this task:\n  [T][ ] read book\n"
                + "Now you have 0 tasks in the list.", elsa.getResponse("delete 1"));
    }

    @Test
    public void getResponse_find_showsMatchesKeepingTheirListNumbers(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");
        elsa.getResponse("todo buy milk");

        assertEquals("Here are the matching tasks in your list:\n2.[T][ ] buy milk",
                elsa.getResponse("find milk"));
    }

    @Test
    public void getResponse_findMatchingNothing_saysSo(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");

        assertEquals("Nothing matching \"zebra\".", elsa.getResponse("find zebra"));
    }

    @Test
    public void getResponse_surroundingSpaces_readTheSameAsWithout(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        assertEquals("Got it. I've added this task:\n  [T][ ] read book\n"
                + "Now you have 1 task in the list.", elsa.getResponse("   todo read book   "));
    }

    // ------------------------------------------------------------------
    // Lines the chatbot cannot carry out
    // ------------------------------------------------------------------

    @Test
    public void getResponse_unknownCommand_complainsInsteadOfThrowing(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        assertEquals(ERROR_PREFIX + "I'm sorry, but I don't know what that means :-(",
                elsa.getResponse("fly"));
    }

    @Test
    public void getResponse_markWithNoTasks_complainsInsteadOfThrowing(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        String response = elsa.getResponse("mark 1");
        assertTrue(response.startsWith(ERROR_PREFIX), "a complaint, not a confirmation");
        assertTrue(response.contains("no tasks yet"), "says why it could not be done");
    }

    @Test
    public void getResponse_taskNumberPastTheEnd_complainsInsteadOfThrowing(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");

        String response = elsa.getResponse("delete 5");
        assertTrue(response.startsWith(ERROR_PREFIX), "a complaint, not a confirmation");
        assertTrue(response.contains("no task 5"), "names the number that was wrong");
    }

    @Test
    public void getResponse_deadlineWithoutADate_complainsInsteadOfThrowing(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        String response = elsa.getResponse("deadline return book");
        assertTrue(response.startsWith(ERROR_PREFIX), "a complaint, not a confirmation");
        assertTrue(response.contains("deadline <description> /by <date>"), "shows how to write it");
    }

    // ------------------------------------------------------------------
    // Ending a session
    // ------------------------------------------------------------------

    @Test
    public void isExiting_beforeSayingGoodbye_isFalse(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("todo read book");

        assertFalse(elsa.isExiting(), "adding a task does not end the session");
    }

    @Test
    public void getResponse_bye_saysFarewellAndEndsTheSession(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();

        assertEquals(FAREWELL, elsa.getResponse("bye"));
        assertTrue(elsa.isExiting(), "the window is told it may close");
    }

    @Test
    public void isExiting_afterACommandThatFailed_staysFalse(@TempDir Path folder) {
        Elsa elsa = elsaIn(folder);
        elsa.startSession();
        elsa.getResponse("fly");

        assertFalse(elsa.isExiting(), "a command that could not run does not end the session");
    }

    // ------------------------------------------------------------------
    // Keeping the tasks between sessions
    // ------------------------------------------------------------------

    @Test
    public void getResponse_addThenStartAgain_findsTheTaskStillThere(@TempDir Path folder) {
        Elsa first = elsaIn(folder);
        first.startSession();
        first.getResponse("todo read book");
        first.getResponse("mark 1");

        // A second chatbot on the same file stands in for the window being
        // closed and opened again.
        Elsa second = elsaIn(folder);
        second.startSession();

        assertEquals("Here are the tasks in your list:\n1.[T][X] read book",
                second.getResponse("list"));
    }
}
