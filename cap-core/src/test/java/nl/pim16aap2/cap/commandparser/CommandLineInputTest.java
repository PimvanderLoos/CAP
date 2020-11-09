package nl.pim16aap2.cap.commandparser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CommandLineInputTest
{
    @Test
    void split()
    {
        Assertions.assertEquals(5, CommandLineInput.split("bigdoors addowner mydoor --player pim16aap2").size());
        Assertions.assertEquals(5, CommandLineInput.split("bigdoors addowner mydoor --player    pim16aap2").size());

        final List<String> split = CommandLineInput.split("bigdoors addowner mydoor --player    pim16aap2   a ");

        Assertions.assertEquals(split.size(), 6);
        // Check that the last entry is "a "
        Assertions.assertEquals(2, split.get(5).length());
    }
}
