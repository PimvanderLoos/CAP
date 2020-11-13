package nl.pim16aap2.cap.argument.parser;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static nl.pim16aap2.cap.util.UtilsForTesting.*;

class ValuelessParserTest
{
    @SneakyThrows
    @Test
    void parseArgument()
    {
        Assertions.assertTrue(
            ValuelessParser.create(true).parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, ""));
        Assertions.assertFalse(
            ValuelessParser.create(false).parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, ""));
    }
}
