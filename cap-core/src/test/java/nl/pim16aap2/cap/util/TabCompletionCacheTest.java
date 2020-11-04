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

        // Make sure that all suggestions are returned properly.
        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));
        @NonNull List<String> output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(1, usedSupplier);

        // Make sure that the suggestions didn't get corrupted between retrievals.
        // So it should give the same output for the same input without quering the supplier method.
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(1, usedSupplier);

        // Ensure that the cache can properly create subselections from the cached suggestions from
        // new data without querying the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test", () -> supplier(suggestionsA, "test"));
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(1, usedSupplier);

        // Ensure the the cache will use the supplier if it cannot generate suggestions from the cached data.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "t"));
        output = tabCompletionCache.getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestionsA, "t"));
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that making another subselection still works as intended (correct results, supplier not queried).
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "tt"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "tt", () -> supplier(suggestionsA, "tt"));
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that going back one character and then typing something else returns the
        // correct results without querying the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "test"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test", () -> supplier(suggestionsA, "test"));
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make a subselection to prepare for the next test.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "testCo"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "testCo", () -> supplier(suggestionsA, "testCo"));
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that going back two characters and then typing something else returns the
        // correct results without querying the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "test"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test", () -> supplier(suggestionsA, "test"));
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that an invalid input does not generate any suggestions and doesn't query the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "testa"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "testa", () -> supplier(suggestionsA, "testa"));
        Assertions.assertEquals(0, output.size());
        Assertions.assertEquals(2, usedSupplier);
    }
}
