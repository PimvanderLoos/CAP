package nl.pim16aap2.cap;

import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CAPTest
{
    @Test
    void split()
    {
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player pim16aap2").size());
        Assertions.assertEquals(5, CAP.split("bigdoors addowner mydoor --player    pim16aap2").size());

        final @NonNull List<String> split = CAP.split("bigdoors addowner mydoor --player    pim16aap2   a ");
        Assertions.assertEquals(split.size(), 6);
        Assertions.assertEquals(2, split.get(5).length());
    }

    @Test
    void caseSensitive()
    {
        @NonNull CAP cap =
            CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').caseSensitive(true).build();
        Assertions.assertEquals("caseSensitive", cap.getCommandNameCaseCheck("caseSensitive"));

        cap.addCommand(Command.commandBuilder().name("commanda").virtual(true).cap(cap).build());
        cap.addCommand(Command.commandBuilder().name("commandB").virtual(true).cap(cap).build());

        Assertions.assertTrue(cap.getCommand("commanda").isPresent());
        Assertions.assertFalse(cap.getCommand("commandA").isPresent());

        Assertions.assertFalse(cap.getCommand("commandb").isPresent());
        Assertions.assertTrue(cap.getCommand("commandB").isPresent());
    }

    @Test
    void caseInsensitive()
    {
        @NonNull CAP cap =
            CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').caseSensitive(false).build();
        Assertions.assertEquals("caseinsensitive", cap.getCommandNameCaseCheck("caseInsensitive"));

        cap.addCommand(Command.commandBuilder().name("commanda").virtual(true).cap(cap).build());
        cap.addCommand(Command.commandBuilder().name("commandB").virtual(true).cap(cap).build());

        Assertions.assertTrue(cap.getCommand("commanda").isPresent());
        Assertions.assertTrue(cap.getCommand("commandA").isPresent());

        Assertions.assertTrue(cap.getCommand("commandb").isPresent());
        Assertions.assertTrue(cap.getCommand("commandB").isPresent());
    }
}
