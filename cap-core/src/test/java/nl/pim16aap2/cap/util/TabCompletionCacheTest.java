package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.SneakyThrows;
import nl.pim16aap2.cap.commandsender.DefaultCommandSender;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class TabCompletionCacheTest
{
    private final @NonNull ICommandSender commandSender = new DefaultCommandSender();

    /**
     * List of potential suggestions.
     */
    private final List<String> suggestionsA = new ArrayList<>(Arrays.asList(
        "testCommandA", "testCommandB", "tttttttt", "test", "testt"));

    /**
     * Keeps track of the number of times the supplier function was used instead of the cache.
     */
    private int usedSupplier = 0;

    /**
     * Selects a list of Strings starting with a certain prefix from another List.
     *
     * @param supply The list of Strings to choose from.
     * @param prefix The prefix each string in the returned list must {@link String#startsWith(String)}.
     * @return A new list of Strings starting with the prefix.
     */
    private @NonNull List<String> supplier(final @NonNull List<String> supply, final @NonNull String prefix)
    {
        usedSupplier += 1;
        List<String> ret = new ArrayList<>();
        supply.forEach(val ->
                       {
                           if (val.startsWith(prefix)) ret.add(val);
                       });
        return ret;
    }

    /**
     * Make sure that the cache is queried exactly as often as it should and its returned values are correct.
     */
    @SneakyThrows
    @Test
    void getTabCompleteOptions()
    {
        final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

        System.out.println("\n0:");
        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));
        @NonNull List<String> output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(1, usedSupplier);

        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(1, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test"));
        output = tabCompletionCache.getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "test"));
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(1, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "t"));
        output = tabCompletionCache.getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(2, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "t"));
        output = tabCompletionCache.getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(2, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "tt"));
        output = tabCompletionCache.getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "tt"));
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(2, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "test"));
        output = tabCompletionCache.getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "test"));
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(3, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "testCom"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, () -> supplier(suggestionsA, "testCom"));
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals(3, usedSupplier);
    }
}
