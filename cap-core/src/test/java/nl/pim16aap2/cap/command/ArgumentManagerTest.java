package nl.pim16aap2.cap.command;

import lombok.NonNull;
import nl.pim16aap2.cap.Localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.Localization.Localizer;
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
            new IntegerArgument().getOptional().identifier("argumenta").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumenta").label("argumenta").build()).build(),

            new IntegerArgument().getOptional().identifier("argumentB").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumentB").label("argumentB").build()).build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(new Localizer.Disabled(), arguments, true);
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
            new IntegerArgument().getOptional().identifier("argumenta").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumenta").label("argumenta").build()).build(),

            new IntegerArgument().getOptional().identifier("argumentB").nameSpec(
                ArgumentNamingSpec.RawStrings.builder().shortName("argumentB").label("argumentB").build()).build()
        );

        final @NonNull ArgumentManager argumentManager = new ArgumentManager(new Localizer.Disabled(),
                                                                             arguments, false);
        Assertions.assertEquals("caseinsensitive", argumentManager.getArgumentNameCaseCheck("caseInsensitive"));

        Assertions.assertTrue(argumentManager.getArgument("argumenta", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentA", (Locale) null).isPresent());

        Assertions.assertTrue(argumentManager.getArgument("argumentb", (Locale) null).isPresent());
        Assertions.assertTrue(argumentManager.getArgument("argumentB", (Locale) null).isPresent());
    }
}
