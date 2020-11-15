package nl.pim16aap2.cap;

import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.util.UtilsForTesting;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class CAPTest
{
    @Test
    void caseSensitive()
    {
        @NonNull CAP cap =
            CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').caseSensitive(true).build();
        Assertions.assertEquals("caseSensitive", cap.getCommandNameCaseCheck("caseSensitive"));

        cap.addCommand(Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commanda"))
                              .virtual(true).cap(cap).build());
        cap.addCommand(Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commandB"))
                              .virtual(true).cap(cap).build());

        Assertions.assertTrue(cap.getCommand("commanda", null).isPresent());
        Assertions.assertFalse(cap.getCommand("commandA", null).isPresent());

        Assertions.assertFalse(cap.getCommand("commandb", null).isPresent());
        Assertions.assertTrue(cap.getCommand("commandB", null).isPresent());
    }

    @Test
    void caseInsensitive()
    {
        @NonNull CAP cap =
            CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').caseSensitive(false).build();
        Assertions.assertEquals("caseinsensitive", cap.getCommandNameCaseCheck("caseInsensitive"));

        cap.addCommand(Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commanda"))
                              .virtual(true).cap(cap).build());
        cap.addCommand(Command.commandBuilder().nameSpec(UtilsForTesting.getBasicCommandName("commandB"))
                              .virtual(true).cap(cap).build());

        Assertions.assertTrue(cap.getCommand("commanda", null).isPresent());
        Assertions.assertTrue(cap.getCommand("commandA", null).isPresent());

        Assertions.assertTrue(cap.getCommand("commandb", null).isPresent());
        Assertions.assertTrue(cap.getCommand("commandB", null).isPresent());
    }
}
