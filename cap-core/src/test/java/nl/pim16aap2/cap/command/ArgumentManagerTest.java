package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class ArgumentManagerTest
{
    @Test
    void caseSensitive()
    {
        final @NonNull List<Argument<?>> arguments = Arrays.asList(
            new IntegerArgument().getOptional().name("argumenta").label("argumenta").summary("").build(),
            new IntegerArgument().getOptional().name("argumentB").label("argumentB").summary("").build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(arguments, true);
        Assertions.assertEquals("caseSensitive", argumentManager.getArgumentNameCaseCheck("caseSensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta").isPresent());
        Assertions.assertFalse(argumentManager.getArgument("argumentA").isPresent());

        Assertions.assertFalse(argumentManager.getArgument("argumentb").isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB").isPresent());
    }

    @Test
    void caseInsensitive()
    {
        final @NonNull List<Argument<?>> arguments = Arrays.asList(
            new IntegerArgument().getOptional().name("argumenta").label("argumenta").summary("").build(),
            new IntegerArgument().getOptional().name("argumentB").label("argumentB").summary("").build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(arguments, false);
        Assertions.assertEquals("caseinsensitive", argumentManager.getArgumentNameCaseCheck("caseInsensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta").isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentA").isPresent());

        Assertions.assertTrue(argumentManager.getArgument("argumentb").isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB").isPresent());
    }
}
