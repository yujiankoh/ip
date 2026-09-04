package elsa.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import elsa.ElsaException;
import elsa.command.AddCommand;
import elsa.command.DeleteCommand;
import elsa.command.ExitCommand;
import elsa.command.FindCommand;
import elsa.command.ListCommand;
import elsa.command.MarkCommand;
import elsa.command.OnCommand;
import elsa.command.UnmarkCommand;
import elsa.task.TaskFormat;

/**
 * Tests {@link Parser#parse}, which turns a typed line into a command.
 *
 * <p>The parser is where every wrong thing a user can type is caught, so most of
 * these cases are about refusal rather than success. What a command then does is
 * not tested here: parse only builds it, and running one needs a task list, a
 * user interface and a store, which is the kind of test the text UI suite already
 * does from the outside.
 *
 * <p>Success cases therefore check only which kind of command came back. That is
 * the parser's real decision, and it is enough to catch a line of the switch
 * wired to the wrong class, which is a mistake no test of the command classes
 * themselves would notice.
 */
public class ParserTest {

    // ------------------------------------------------------------------
    // Which command a line names
    // ------------------------------------------------------------------

    @Test
    public void parse_bye_returnsExitCommand() throws ElsaException {
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    public void parse_list_returnsListCommand() throws ElsaException {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    public void parse_todo_returnsAddCommand() throws ElsaException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book"));
    }

    @Test
    public void parse_deadline_returnsAddCommand() throws ElsaException {
        assertInstanceOf(AddCommand.class, Parser.parse("deadline return book /by 2019-10-15"));
    }

    @Test
    public void parse_event_returnsAddCommand() throws ElsaException {
        assertInstanceOf(AddCommand.class,
                Parser.parse("event meeting /from 2019-10-14 /to 2019-10-16"));
    }

    /**
     * Marking and unmarking are opposites written almost identically, so a line of
     * the switch pointing at the wrong one of them is an easy mistake to make and
     * an easy one to miss.
     */
    @Test
    public void parse_markAndUnmark_returnTheirOwnCommands() throws ElsaException {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 2"));
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 2"));
    }

    @Test
    public void parse_delete_returnsDeleteCommand() throws ElsaException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 2"));
    }

    @Test
    public void parse_on_returnsOnCommand() throws ElsaException {
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-10-15"));
    }

    @Test
    public void parse_find_returnsFindCommand() throws ElsaException {
        assertInstanceOf(FindCommand.class, Parser.parse("find book"));
    }

    /** A keyword may be several words, so the rest of the line is taken as it is. */
    @Test
    public void parse_findWithSeveralWords_returnsFindCommand() throws ElsaException {
        assertInstanceOf(FindCommand.class, Parser.parse("find read book"));
    }

    @Test
    public void parse_findWithoutAKeyword_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("find"));
        assertThrows(ElsaException.class, () -> Parser.parse("find    "));
    }

    /**
     * Unlike a description, a keyword is never stored, so text that could not be
     * saved as a task is still allowed to be searched for.
     */
    @Test
    public void parse_findWithTheFieldSeparator_isAccepted() throws ElsaException {
        assertInstanceOf(FindCommand.class,
                Parser.parse("find read" + TaskFormat.SEPARATOR + "book"));
    }

    /** Only leaving ends the session, whatever else was typed. */
    @Test
    public void isExit_onlyTheByeCommand_returnsTrue() throws ElsaException {
        assertTrue(Parser.parse("bye").isExit());
        assertFalse(Parser.parse("list").isExit());
        assertFalse(Parser.parse("todo read book").isExit());
    }

    // ------------------------------------------------------------------
    // How a line is split
    // ------------------------------------------------------------------

    @Test
    public void parse_surroundingAndRepeatedSpaces_stillFindsTheCommand() throws ElsaException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo    read book   "));
    }

    /** A tab separates a command from its argument just as a space does. */
    @Test
    public void parse_tabBetweenCommandAndArgument_stillFindsTheCommand() throws ElsaException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo\tread book"));
    }

    @Test
    public void parse_wrongLetterCase_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("BYE"));
        assertThrows(ElsaException.class, () -> Parser.parse("Todo read book"));
    }

    @Test
    public void parse_wordThatIsNoCommand_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("blah"));
    }

    /**
     * An empty line is told apart from an unknown command, so that the user is
     * told they typed nothing rather than that their nothing was not understood.
     */
    @Test
    public void parse_emptyLine_throwsExceptionSayingNothingWasTyped() {
        ElsaException thrown = assertThrows(ElsaException.class, () -> Parser.parse(""));
        assertTrue(thrown.getMessage().contains("did not type anything"));
    }

    // ------------------------------------------------------------------
    // Adding a task: parts missing or empty
    // ------------------------------------------------------------------

    @Test
    public void parse_todoWithoutDescription_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("todo"));
        assertThrows(ElsaException.class, () -> Parser.parse("todo    "));
    }

    @Test
    public void parse_deadlineWithoutTheBySeparator_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("deadline return book"));
    }

    @Test
    public void parse_deadlineWithoutADateAfterBy_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("deadline return book /by"));
        assertThrows(ElsaException.class, () -> Parser.parse("deadline return book /by    "));
    }

    @Test
    public void parse_deadlineWithoutADescription_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("deadline /by 2019-10-15"));
    }

    @Test
    public void parse_eventWithoutTheFromSeparator_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("event meeting /to 2019-10-16"));
    }

    @Test
    public void parse_eventWithoutTheToSeparator_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("event meeting /from 2019-10-14"));
    }

    @Test
    public void parse_eventWithAnEmptyDate_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("event meeting /from /to 2019-10-16"));
        assertThrows(ElsaException.class, () -> Parser.parse("event meeting /from 2019-10-14 /to"));
    }

    // ------------------------------------------------------------------
    // Adding a task: parts that are there but wrong
    // ------------------------------------------------------------------

    @Test
    public void parse_deadlineWithTextInsteadOfADate_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("deadline return book /by someday"));
    }

    @Test
    public void parse_deadlineWithAnImpossibleDate_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("deadline return book /by 31/2/2019"));
    }

    /**
     * The message a bad date produces has to reach the user with the usage of the
     * command added, so they can see both what was wrong and how the line should
     * have been written.
     */
    @Test
    public void parse_badDate_messageSaysHowToWriteTheCommand() {
        String command = "deadline return book /by someday";
        ElsaException thrown = assertThrows(ElsaException.class, () -> Parser.parse(command));
        assertTrue(thrown.getMessage().contains("deadline <description> /by <date>"));
    }

    @Test
    public void parse_onWithoutADate_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("on"));
    }

    @Test
    public void parse_onWithTextInsteadOfADate_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("on someday"));
    }

    // ------------------------------------------------------------------
    // Task numbers
    // ------------------------------------------------------------------

    @Test
    public void parse_markWithoutANumber_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("mark"));
    }

    @Test
    public void parse_markWithSomethingThatIsNotANumber_throwsException() {
        assertThrows(ElsaException.class, () -> Parser.parse("mark two"));
        assertThrows(ElsaException.class, () -> Parser.parse("mark 1.5"));
        assertThrows(ElsaException.class, () -> Parser.parse("delete 1 2"));
    }

    /**
     * Whether a task with that number exists depends on how many tasks there are,
     * which the parser has no way of knowing, so a number that is merely out of
     * range is accepted here and refused later by the command itself.
     */
    @Test
    public void parse_numberThatCouldNotExist_isStillAcceptedHere() throws ElsaException {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 999"));
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 0"));
        assertInstanceOf(MarkCommand.class, Parser.parse("mark -1"));
    }

    // ------------------------------------------------------------------
    // Text that could not be stored
    // ------------------------------------------------------------------

    /**
     * A description holding the text that separates the fields of a saved task
     * would be split into extra fields when the file is read back, so the task
     * would come back changed or not at all. It is refused now, while the user is
     * still there to be told, rather than losing part of their task later.
     */
    @Test
    public void parse_descriptionContainingTheFieldSeparator_throwsException() {
        String separator = TaskFormat.SEPARATOR;
        assertThrows(ElsaException.class, () -> Parser.parse("todo read" + separator + "book"));
        assertThrows(ElsaException.class, () -> Parser.parse("deadline read" + separator + "book /by 2019-10-15"));
    }

    /** The separator is a bar with a space on each side, so a bare bar is fine. */
    @Test
    public void parse_descriptionContainingABarWithoutSpaces_isAccepted() throws ElsaException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo read a|b"));
    }

    // ------------------------------------------------------------------
    // Dates are read the same way wherever they appear
    // ------------------------------------------------------------------

    @Test
    public void parse_everyAcceptedDateForm_isAcceptedInACommand() throws ElsaException {
        String[] forms = {"2019-10-15", "15/10/2019", "Oct 15 2019", "October 15 2019"};
        for (String form : forms) {
            assertInstanceOf(AddCommand.class, Parser.parse("deadline return book /by " + form),
                    "a deadline could not be written with the date as " + form);
            assertInstanceOf(OnCommand.class, Parser.parse("on " + form),
                    "the on command could not be written with the date as " + form);
        }
    }

    @Test
    public void parse_deadlineWithSpacesAroundTheDate_isAccepted() throws ElsaException {
        assertEquals(AddCommand.class,
                Parser.parse("deadline return book /by   2019-10-15  ").getClass());
    }
}
