package nl.pim16aap2.cap.argument.parser;

import lombok.SneakyThrows;
import nl.pim16aap2.cap.exception.IllegalValueException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static nl.pim16aap2.cap.util.UtilsForTesting.*;

class IntegerParserTest
{
    @SneakyThrows
    @Test
    void parseArgument()
    {
        Assertions.assertEquals(10, IntegerParser.create().parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER,
                                                                         DUMMY_ARGUMENT, "10"));
        Assertions.assertEquals(-999, IntegerParser.create().parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER,
                                                                           DUMMY_ARGUMENT, "-999"));
        Assertions.assertThrows(IllegalValueException.class, () ->
            IntegerParser.create().parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, "-999a"));
        Assertions.assertThrows(IllegalValueException.class, () ->
            IntegerParser.create().parseArgument(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, ""));
    }
}
