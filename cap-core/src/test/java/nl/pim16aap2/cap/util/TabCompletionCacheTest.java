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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class TabCompletionCacheTest
{
    private final @NonNull ICommandSender commandSender = new DefaultCommandSender();

    /**
     * List of potential suggestions.
     */
    private final List<String> suggestions = new ArrayList<>(Arrays.asList(
        "testCommandA", "testCommandB", "tttttttt", "test", "testt"));

    /**
     * List of potential suggestions.
     */
    private final List<String> suggestionsSpaces = new ArrayList<>(Arrays.asList(
        "\"test Command A\"", "\"test Value B\"", "tnoSpacesHere"));

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
                           // Match 'test' against both 'testCommand' and '"test command"'.
                           if (val.startsWith(prefix) || (val.startsWith("\"") && val.substring(1).startsWith(prefix)))
                               ret.add(val);
                       });
        return ret;
    }

    /**
     * Selects a list of Strings starting with a certain prefix from another List. The list is returned after a delay.
     *
     * @param supply The list of Strings to choose from.
     * @param prefix The prefix each string in the returned list must {@link String#startsWith(String)}.
     * @param millis The amount of time (in milliseconds) to wait before returning the value.
     * @return A {@link CompletableFuture} with a new list of Strings starting with the prefix.
     */
    private @NonNull List<String> delayedSupplier(final @NonNull List<String> supply, final @NonNull String prefix,
                                                  final long millis)
    {
        usedSupplier += 1;
        List<String> ret = new ArrayList<>();
        supply.forEach(val ->
                       {
                           if (val.startsWith(prefix))
                               ret.add(val);
                       });
        UtilsForTesting.sleep(millis);
        return ret;
    }

    @SneakyThrows
    @Test
    void testSpaces()
    {
        final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));
        @NonNull List<String> output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestionsSpaces, "t"), false);

        Assertions.assertEquals(3, output.size());
        Assertions.assertEquals("\"test Command A\"", output.get(0));

        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test", () -> supplier(suggestionsSpaces, "test"), false);
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals("\"test Command A\"", output.get(0));

        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "\"test", () -> supplier(suggestionsSpaces, "\"test"), false);
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals("\"test Command A\"", output.get(0));
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
            .getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestions, "t"), false);
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(1, usedSupplier);

        // Make sure that the suggestions didn't get corrupted between retrievals.
        // So it should give the same output for the same input without quering the supplier method.
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestions, "t"), false);
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(1, usedSupplier);

        // Ensure that the cache can properly create subselections from the cached suggestions from
        // new data without querying the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test", () -> supplier(suggestions, "test"), false);
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(1, usedSupplier);

        // Ensure the the cache will use the supplier if it cannot generate suggestions from the cached data.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "t"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "t", () -> supplier(suggestions, "t"), false);
        Assertions.assertEquals(5, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that making another subselection still works as intended (correct results, supplier not queried).
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "tt"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "tt", () -> supplier(suggestions, "tt"), false);
        Assertions.assertEquals(1, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that going back one character and then typing something else returns the
        // correct results without querying the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "test"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test", () -> supplier(suggestions, "test"), false);
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make a subselection to prepare for the next test.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "testCo"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "testCo", () -> supplier(suggestions, "testCo"), false);
        Assertions.assertEquals(2, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that going back two characters and then typing something else returns the
        // correct results without querying the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "test"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "test",
                                   () -> supplier(suggestions, "test"), false);
        Assertions.assertEquals(4, output.size());
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that an invalid input does not generate any suggestions and doesn't query the supplier.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test", "testa"));
        output = tabCompletionCache
            .getTabCompleteOptions(commandSender, input, "testa",
                                   () -> supplier(suggestions, "testa"), false);
        Assertions.assertEquals(0, output.size());
        Assertions.assertEquals(2, usedSupplier);
    }

    @SneakyThrows
    @Test
    void testDelayedSuggestions()
    {
        final TabCompletionCache tabCompletionCache = new TabCompletionCache();

        // Make sure that all suggestions are returned properly.
        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));

        // Make sure that the output is empty while it hasn't been accessed yet.
        @NonNull Optional<List<String>> output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t",
                                          () -> delayedSupplier(suggestions, "t", 30), false);
        Assertions.assertFalse(output.isPresent());
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(1, usedSupplier);

        // Make sure that requesting the same input before the supplier has completed doesn't query the supplier again.
        // It should just wait instead.
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t",
                                          () -> delayedSupplier(suggestions, "t", 30), false);
        Assertions.assertFalse(output.isPresent());
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(1, usedSupplier);

        // Make sure that once the supplier has completed, the result is available without querying the supplier again.
        UtilsForTesting.sleep(35); // Make sure that the CompletableFuture has completed.
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t",
                                          () -> delayedSupplier(suggestions, "t", 30), false);
        Assertions.assertTrue(output.isPresent());
        Assertions.assertEquals(suggestions.size(), output.get().size());
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(1, usedSupplier);

        // Add another argument, this should invalidate the cache (i.e., it should be empty again).
        input = new ArrayList<>(Arrays.asList("mycommand ", "test ", "t"));
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "t",
                                          () -> delayedSupplier(suggestions, "t", 30), false);
        Assertions.assertFalse(output.isPresent());
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(2, usedSupplier);

        // Make sure that after completion, we can retrieve a narrower result without querying the cache again.
        UtilsForTesting.sleep(35); // Make sure that the CompletableFuture has completed.
        input = new ArrayList<>(Arrays.asList("mycommand ", "test ", "testCom"));
        output = tabCompletionCache
            .getDelayedTabCompleteOptions(commandSender, input, "testCom",
                                          () -> delayedSupplier(suggestions, "testCom", 30), false);
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(2, usedSupplier);
        Assertions.assertTrue(output.isPresent());
        Assertions.assertEquals(2, output.get().size());
    }

    @SneakyThrows
    @Test
    void testFutureSuggestions()
    {
        final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

        // Make sure that all suggestions are returned properly.
        @NonNull List<String> input = new ArrayList<>(Arrays.asList("mycommand ", "t"));

        // Place a request that takes 30ms to complete.
        tabCompletionCache
            .getTabCompleteOptionsAsync(commandSender, input, "t", () -> delayedSupplier(suggestions, "t", 30), false);
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(1, usedSupplier);

        // Make sure that placing another request before the 30ms are over doesn't result in querying the supplier again.
        tabCompletionCache
            .getTabCompleteOptionsAsync(commandSender, input, "t", () -> delayedSupplier(suggestions, "t", 30), false);
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(1, usedSupplier);

        // Make sure that the output is correct once the supplier does complete (and that the supplier isn't queried again).
        UtilsForTesting.sleep(35);
        @NonNull CompletableFuture<List<String>> output = tabCompletionCache
            .getTabCompleteOptionsAsync(commandSender, input, "t", () -> delayedSupplier(suggestions, "t", 30), false);
        Assertions.assertEquals(suggestions.size(), output.get(1, TimeUnit.MILLISECONDS).size());
        UtilsForTesting.sleep(1); // Make sure to take any overhead of the async call into account.
        Assertions.assertEquals(1, usedSupplier);
    }
}
