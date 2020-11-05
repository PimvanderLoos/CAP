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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
     * Returns a completablefuture.
     *
     * @param fut The completable future to return.
     * @return A {@link CompletableFuture} with a new list of Strings starting with the prefix.
     */
    private @NonNull CompletableFuture<List<String>> asyncSupplier(final @NonNull CompletableFuture<List<String>> fut)
    {
        usedSupplier += 1;
        return fut;
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

    @SneakyThrows
    @Test
    void testDelayedSuggestions()
    {
        final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

        // Make sure that all suggestions are returned properly.
        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));

        final @NonNull CompletableFuture<List<String>> suggestions = new CompletableFuture<>();
        @NonNull List<String> output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t", () -> asyncSupplier(suggestions));
        Assertions.assertEquals(0, output.size());
        Assertions.assertEquals(1, usedSupplier);

        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t", () -> asyncSupplier(suggestions));
        Assertions.assertEquals(0, output.size());
        Assertions.assertEquals(1, usedSupplier);

        suggestions.complete(suggestionsA);
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t", () -> asyncSupplier(suggestions));
        Assertions.assertEquals(suggestionsA.size(), output.size());
        Assertions.assertEquals(1, usedSupplier);


        input = new ArrayList<>(Arrays.asList("mycommand ", "test ", "t"));
        final @NonNull CompletableFuture<List<String>> newSuggestions = new CompletableFuture<>();
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t", () -> asyncSupplier(newSuggestions));
        Assertions.assertEquals(0, output.size());
        Assertions.assertEquals(2, usedSupplier);

        input = new ArrayList<>(Arrays.asList("mycommand ", "test ", "testCom"));
        newSuggestions.complete(suggestionsA);
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "testCom", () -> asyncSupplier(newSuggestions));
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals(2, usedSupplier);
    }

    @SneakyThrows
    @Test
    void testFutureSuggestions()
    {
        final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

        // Make sure that all suggestions are returned properly.
        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));

        final @NonNull CompletableFuture<List<String>> suggestions = new CompletableFuture<>();
        @NonNull CompletableFuture<List<String>> output = tabCompletionCache
            .getTabCompleteOptionsAsync(commandSender, input, "t", () -> asyncSupplier(suggestions));
        Assertions.assertEquals(output, suggestions);
        Assertions.assertEquals(1, usedSupplier);

        tabCompletionCache.getTabCompleteOptionsAsync(commandSender, input, "t", () -> asyncSupplier(suggestions));
        Assertions.assertEquals(1, usedSupplier);

        suggestions.complete(suggestionsA);
        output = tabCompletionCache
            .getTabCompleteOptionsAsync(commandSender, input, "t", () -> asyncSupplier(suggestions));
        Assertions.assertEquals(suggestionsA.size(), output.get(1, TimeUnit.MILLISECONDS).size());
        Assertions.assertEquals(1, usedSupplier);
    }
}
