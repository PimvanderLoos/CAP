package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.specialized.IntegerArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class ArgumentManagerTest
{
    @Test
    void caseSensitive()
    {
        final @NonNull List<Argument<?>> arguments = Arrays.asList(
            new IntegerArgument().getOptional().shortName("argumenta").identifier("argumenta")
                                 .label("argumenta").summary("").build(),
            new IntegerArgument().getOptional().shortName("argumentB").identifier("argumentB")
                                 .label("argumentB").summary("").build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(CAP.getDefault(), arguments, true);
        Assertions.assertEquals("caseSensitive", argumentManager.getArgumentNameCaseCheck("caseSensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta", (Locale) null).isPresent());
        Assertions.assertFalse(argumentManager.getArgument("argumentA", (Locale) null).isPresent());

        Assertions.assertFalse(argumentManager.getArgument("argumentb", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB", (Locale) null).isPresent());
    }

    @Test
    void caseInsensitive()
    {
        final @NonNull List<Argument<?>> arguments = Arrays.asList(
            new IntegerArgument().getOptional().shortName("argumenta").identifier("argumenta")
                                 .label("argumenta").summary("").build(),
            new IntegerArgument().getOptional().shortName("argumentB").identifier("argumentB")
                                 .label("argumentB").summary("").build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(CAP.getDefault(), arguments, false);
        Assertions.assertEquals("caseinsensitive", argumentManager.getArgumentNameCaseCheck("caseInsensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentA", (Locale) null).isPresent());

        Assertions.assertTrue(argumentManager.getArgument("argumentb", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB", (Locale) null).isPresent());
    }
}
