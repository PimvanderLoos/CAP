package nl.pim16aap2.cap.commandparser;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TabCompletionSuggesterTest
{
    final DefaultCommandSender commandSender = new DefaultCommandSender();

    @Test
    void getCommandTabCompleteOptions()
    {
        final @NonNull CAP cap = CommandParserTest
            .setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());

        Assertions.assertEquals(20, cap.getTabCompleteOptions(commandSender, "bigdoors sub").size());

        Assertions.assertEquals(1, cap.getTabCompleteOptions(commandSender, "bigdoors").size());

        Assertions.assertEquals(23, cap.getTabCompleteOptions(commandSender, "bigdoors ").size());

        Assertions.assertEquals("bigdoors", cap.getTabCompleteOptions(commandSender, "big").get(0));

        Assertions.assertEquals(0, cap.getTabCompleteOptions(commandSender, "sub").size());
    }

    @Test
    void getArgumentNameTabCompleteOptions()
    {
        final @NonNull CAP cap = CommandParserTest
            .setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());

        List<String> suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner door -p");
        Assertions.assertEquals(2, suggestions.size());
        Assertions.assertEquals("-p=", suggestions.get(0));
        Assertions.assertEquals("--player=", suggestions.get(1));

        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner -p");
        Assertions.assertEquals(0, suggestions.size());

        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner door --pla");
        Assertions.assertEquals(1, suggestions.size());
        Assertions.assertEquals("--player=", suggestions.get(0));

        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner door --ad");
        Assertions.assertEquals(1, suggestions.size());
        Assertions.assertEquals("--admin", suggestions.get(0));

        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner mydoor --admin ");
        Assertions.assertEquals(6, suggestions.size());


        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors subcommand_0");
        Assertions.assertEquals(1, suggestions.size());
        Assertions.assertEquals("subcommand_0", suggestions.get(0));

        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors subcommand_0 ");
        Assertions.assertEquals(1, suggestions.size());
        Assertions.assertEquals("-v=", suggestions.get(0));

        suggestions = cap.getTabCompleteOptions(commandSender, "bigdoors help subcommand_1");
        Assertions.assertEquals(11, suggestions.size());
    }

    private void getFreeArgumentValueTabCompleteOptions(final @NonNull CAP cap)
    {
        List<String> suggestions = cap.getTabCompleteOptions(commandSender, String.format("bigdoors addowner door -p%c",
                                                                                          cap.getSeparator()));
        Assertions.assertEquals(4, suggestions.size());

        suggestions = cap.getTabCompleteOptions(commandSender, String.format("bigdoors addowner door -p%cpim",
                                                                             cap.getSeparator()));
        Assertions.assertEquals(3, suggestions.size());

        // Make sure that the suggestions properly have the flag if needed.
        // Space-separated arguments don't need a prefix (they are not part of the suggestion).
        @NonNull String argumentFlag = cap.getSeparator() == ' ' ? "" : String.format("-p%c", cap.getSeparator());

        suggestions = cap.getTabCompleteOptions(commandSender, String.format("bigdoors addowner door -p%cpim16",
                                                                             cap.getSeparator()));
        Assertions.assertEquals(2, suggestions.size());


        Assertions.assertEquals(argumentFlag + "pim16aap2", suggestions.get(0));
        Assertions.assertEquals(argumentFlag + "pim16aap3", suggestions.get(1));

        // Also test the long flag
        argumentFlag = cap.getSeparator() == ' ' ? "" : String.format("--player%c", cap.getSeparator());
        suggestions = cap.getTabCompleteOptions(commandSender, String.format("bigdoors addowner door --player%cpim16",
                                                                             cap.getSeparator()));
        Assertions.assertEquals(2, suggestions.size());
        Assertions.assertEquals(argumentFlag + "pim16aap2", suggestions.get(0));
        Assertions.assertEquals(argumentFlag + "pim16aap3", suggestions.get(1));
    }

    @Test
    void getFreeArgumentValueTabCompleteOptions()
    {
        getFreeArgumentValueTabCompleteOptions(
            CommandParserTest.setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build()));
    }

    @Test
    void getFreeArgumentValueTabCompleteOptionsSpaceSeparator()
    {
        final @NonNull CAP cap = CommandParserTest
            .setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator(' ').build());
        getFreeArgumentValueTabCompleteOptions(cap);

        final @NonNull List<String> playerNameSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner flag --player  ");
        Assertions.assertEquals(4, playerNameSuggestions.size());
    }

    @Test
    void getPositionalValueTabCompleteOptions()
    {
        final @NonNull CAP cap = CommandParserTest
            .setUp(CAP.getDefault().toBuilder().exceptionHandler(null).separator('=').build());

        List<String> doorIDSuggestions =
            cap.getTabCompleteOptions(commandSender, "bigdoors addowner 4");

        Assertions.assertEquals(1, doorIDSuggestions.size());
        Assertions.assertEquals("42", doorIDSuggestions.get(0));

        doorIDSuggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner my");
        Assertions.assertEquals(2, doorIDSuggestions.size());
        Assertions.assertEquals("myDoor", doorIDSuggestions.get(0));
        Assertions.assertEquals("\"my Portcullis\"", doorIDSuggestions.get(1));

        doorIDSuggestions = cap.getTabCompleteOptions(commandSender, "bigdoors addowner ");
        Assertions.assertEquals(4, doorIDSuggestions.size());
        Assertions.assertEquals("myDoor", doorIDSuggestions.get(0));
        Assertions.assertEquals("42", doorIDSuggestions.get(1));
    }
}
