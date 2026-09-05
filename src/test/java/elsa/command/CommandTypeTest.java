package elsa.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests {@link CommandType}, the list of words the chatbot understands.
 *
 * <p>Nearly all of the behaviour is in {@link CommandType#fromKeyword}, which
 * decides whether a word names a command at all. Two of its answers are special:
 * an empty line is NOTHING rather than UNKNOWN, so the user can be told they
 * typed nothing instead of being told their nothing was not understood; and
 * UNKNOWN itself has a null keyword so that no input can match it by accident.
 */
public class CommandTypeTest {

    @Test
    public void fromKeyword_everyRealKeyword_findsItsOwnCommand() {
        for (CommandType command : CommandType.values()) {
            if (command == CommandType.UNKNOWN) {
                continue;
            }
            assertEquals(command, CommandType.fromKeyword(command.getKeyword()),
                    "the keyword of " + command + " no longer finds it");
        }
    }

    /**
     * The window introduces itself with this list, so a command missing from it
     * is one a new user has no way of discovering.
     */
    @Test
    public void getUsages_everyCommandAUserCanType_isListed() {
        List<String> usages = CommandType.getUsages();

        for (CommandType command : CommandType.values()) {
            boolean isTypeable = command != CommandType.NOTHING && command != CommandType.UNKNOWN;
            if (isTypeable) {
                assertTrue(usages.contains(command.getUsage()),
                        command + " is missing from the list shown to the user");
            }
        }
    }

    /**
     * NOTHING and UNKNOWN are what the chatbot calls a blank line and a word it
     * does not know. Listing either would tell the user to type something that
     * is not a command.
     */
    @Test
    public void getUsages_nothingAndUnknown_areLeftOut() {
        List<String> usages = CommandType.getUsages();

        assertEquals(CommandType.values().length - 2, usages.size(),
                "exactly the two commands nobody types should be left out");
        assertFalse(usages.contains(""), "NOTHING has no usage and must not be listed");
        assertFalse(usages.contains(null), "UNKNOWN has no usage and must not be listed");
    }

    /** A user should meet the commands that put a task in before the one that leaves. */
    @Test
    public void getUsages_theOrderShown_startsWithAddingAndEndsWithLeaving() {
        List<String> usages = CommandType.getUsages();

        assertEquals(CommandType.TODO.getUsage(), usages.get(0));
        assertEquals(CommandType.BYE.getUsage(), usages.get(usages.size() - 1));
    }

    @Test
    public void fromKeyword_knownKeyword_returnsThatCommand() {
        assertEquals(CommandType.BYE, CommandType.fromKeyword("bye"));
        assertEquals(CommandType.DEADLINE, CommandType.fromKeyword("deadline"));
    }

    @Test
    public void fromKeyword_unknownWord_returnsUnknown() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromKeyword("blah"));
    }

    /**
     * Keywords are matched exactly, so a command typed in the wrong case is not
     * that command. The text UI tests check the same thing from the outside: BYE
     * does not end the session.
     */
    @Test
    public void fromKeyword_rightWordWrongCase_returnsUnknown() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromKeyword("BYE"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromKeyword("Todo"));
    }

    /** An empty line has its own answer, so the user can be told they typed nothing. */
    @Test
    public void fromKeyword_emptyString_returnsNothing() {
        assertEquals(CommandType.NOTHING, CommandType.fromKeyword(""));
    }

    /**
     * UNKNOWN carries a null keyword precisely so that nothing can match it. This
     * checks the search does not fall over when it reaches that constant, which it
     * does on every unmatched word.
     */
    @Test
    public void fromKeyword_wordThatMatchesNothing_doesNotFailOnTheNullKeyword() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromKeyword("zzz"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromKeyword("null"));
    }

    /**
     * The usage is what the user is shown when a command is typed wrongly, so it
     * has to begin with the command they typed. A usage left over from another
     * command would send them to the wrong place, which is the mistake this pairing
     * of keyword and usage in one constant exists to prevent.
     */
    @Test
    public void getUsage_everyRealCommand_beginsWithItsOwnKeyword() {
        for (CommandType command : CommandType.values()) {
            if (command == CommandType.UNKNOWN || command == CommandType.NOTHING) {
                continue;
            }
            assertNotNull(command.getUsage(), command + " has no usage");
            assertTrue(command.getUsage().startsWith(command.getKeyword()),
                    "the usage of " + command + " does not begin with its keyword: "
                            + command.getUsage());
        }
    }
}
