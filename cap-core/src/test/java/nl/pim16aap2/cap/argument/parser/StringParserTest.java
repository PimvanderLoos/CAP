package nl.pim16aap2.cap.argument.parser;

import lombok.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static nl.pim16aap2.cap.util.UtilsForTesting.*;

class StringParserTest
{
    private void test(final @NonNull String str)
    {
        Assertions.assertEquals(str, StringParser.create().parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER,
                                                                         DUMMY_ARGUMENT, str));
    }

    @Test
    void parseArgument()
    {
        Arrays.asList("TEST", "this is a longer TEST").forEach(this::test);
    }
}
