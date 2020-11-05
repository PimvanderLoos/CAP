package nl.pim16aap2.cap.util;

import lombok.NonNull;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.util.Functional.CheckedSupplier;
import nl.pim16aap2.cap.util.cache.TimedCache;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Represents a cache for tab completion suggestions. Once a list of suggestions is created for an {@link
 * ICommandSender}, this list will be used for future lookups, if possible. Once a list of String suggestions is
 * constructed, we don't have to recalculate all the options if the partial match increased in size, but still starts
 * with the same characters as used for the last lookup. If this is the case, we can just remove all entries from the
 * list that do not start with the new partial match.
 * <p>
 * This is especially useful when suggesting items from a list obtained via an expensive operation.
 * <p>
 * For example, a list of values from a database. On the Spigot platform, the suggestions are recalculated every time
 * the user, so caching them means that getting a name of 10 characters from it only requires a single lookup instead of
 * 10.
 * <p>
 * The suggestions are cached for 2 minutes using a {@link TimedCache}.
 *
 * @author Pim
 */
public class TabCompletionCache
{
    private final @NonNull TimedCache<ICommandSender, CacheEntry> tabCompletionCache =
        TimedCache.<ICommandSender, CacheEntry>builder()
            .duration(Duration.ofMinutes(2))
            .cleanup(Duration.ofMinutes(5))
            .softReference(true)
            .refresh(true)
            .build();

    /**
     * Gets the list of suggested tab complete options for an {@link ICommandSender} base on the current arguments.
     *
     * @param commandSender The {@link ICommandSender} for which to get the list of suggested tab completions.
     * @param args          The current list of arguments.
     * @param lastArg       The last argument in the command. This may or may not be the last entry in the list of
     *                      arguments, but the parser can figure that out.
     * @param fun           The function to retrieve the list of arguments if they cannot be retrieved from cache.
     * @return The list of suggested tab completions.
     *
     * @throws EOFException If the command contains unmatched quotation marks. E.g. '<i>--player="pim 16aap2</i>'.
     */
    public @NonNull List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                       final @NonNull List<String> args,
                                                       final @NonNull String lastArg,
                                                       final @NonNull CheckedSupplier<List<String>, EOFException> fun)
        throws EOFException
    {
        final @NonNull CacheEntry cacheEntry =
            tabCompletionCache.get(commandSender)
                              .orElseGet(() -> tabCompletionCache.put(commandSender, new CacheEntry()));

        final @NonNull Optional<List<String>> suggestions = cacheEntry.suggestionsSubSelection(args.size(), lastArg);
        if (suggestions.isPresent())
            return suggestions.get();


        final @NonNull List<String> newSuggestions = fun.get();
        cacheEntry.reset(newSuggestions, args.size(), lastArg);
        return newSuggestions;
    }

    /**
     * Gets the list of suggested tab complete options for an {@link ICommandSender} base on the current arguments.
     * <p>
     * If the results are not cached, the results will be obtained using an asynchronous method.
     *
     * @param commandSender The {@link ICommandSender} for which to get the list of suggested tab completions.
     * @param args          The current list of arguments.
     * @param lastArg       The last argument in the command. This may or may not be the last entry in the list of
     *                      arguments, but the parser can figure that out.
     * @param fun           The function to retrieve the {@link CompletableFuture} list of arguments if they cannot be
     *                      retrieved from cache.
     * @return The {@link CompletableFuture} of the list of suggested tab completions.
     */
    public @NonNull CompletableFuture<List<String>> getTabCompleteOptionsAsync(
        final @NonNull ICommandSender commandSender, final @NonNull List<String> args, final @NonNull String lastArg,
        final @NonNull Supplier<List<String>> fun)
    {
        final @NonNull Pair<List<String>, CompletableFuture<List<String>>> result =
            getAsyncCachedEntrySuggestions(commandSender, args, lastArg, fun);

        if (result.first != null)
            return CompletableFuture.completedFuture(result.first);
        return result.second;
    }

    /**
     * Gets the list of suggested tab complete options for an {@link ICommandSender} base on the current arguments.
     * <p>
     * If the results are not cached, the results will be put in the cache using an asynchronous method.
     * <p>
     * Unlike {@link #getTabCompleteOptionsAsync(ICommandSender, List, String, Supplier)}, this method will not return a
     * {@link CompletableFuture} with the results if they had to be retrieved async, but instead, it will return an
     * empty list.
     * <p>
     * Successive calls will keep returning empty lists until the async supplier has supplied the cache with a result.
     * From then on, it will retrieve any values
     *
     * @param commandSender The {@link ICommandSender} for which to get the list of suggested tab completions.
     * @param args          The current list of arguments.
     * @param lastArg       The last argument in the command. This may or may not be the last entry in the list of
     *                      arguments, but the parser can figure that out.
     * @param fun           The function to retrieve the {@link CompletableFuture} list of arguments if they cannot be
     *                      retrieved from cache.
     * @return The list of suggested tab completions if one could be found. If no results are in the cache yet an empty
     * list is returned.
     */
    public @NonNull List<String> getDelayedTabCompleteOptions(
        final @NonNull ICommandSender commandSender, final @NonNull List<String> args, final @NonNull String lastArg,
        final @NonNull Supplier<List<String>> fun)
    {
        final @NonNull Pair<List<String>, CompletableFuture<List<String>>> result =
            getAsyncCachedEntrySuggestions(commandSender, args, lastArg, fun);

        System.out.println("is first null? " + (result.first == null));
        if (result.first != null)
            return result.first;
        return new ArrayList<>(0);
    }

    /**
     * Gets the list of tab completion suggestions from an async supplier.
     * <p>
     * If no entry exists in the cache for the provided {@link ICommandSender}, a new entry will be created with a new
     * {@link AsyncCacheEntry} as its value.
     * <p>
     * If the entry does exist, it will be used to narrow down the previously-obtained suggestions using the provided
     * lastArg.
     *
     * @param commandSender The {@link ICommandSender} for which to get the list of suggested tab completions.
     * @param args          The current list of arguments.
     * @param lastArg       The last argument in the command. This may or may not be the last entry in the list of
     *                      arguments, but the parser can figure that out.
     * @param fun           The function to retrieve the {@link CompletableFuture} list of arguments if they cannot be
     *                      retrieved from cache.
     * @return Either a list of suggestions or a {@link CompletableFuture} with the suggestions. Only a single value is
     * returned and the other value is always null. So if a list of suggestions could be found, those will be returned
     * and null for the future one. If no list of suggestions could be found, the future suggestions will be returned
     * and the list will be null.
     */
    private @NonNull Pair<List<String>, CompletableFuture<List<String>>> getAsyncCachedEntrySuggestions(
        final @NonNull ICommandSender commandSender, final @NonNull List<String> args, final @NonNull String lastArg,
        final @NonNull Supplier<List<String>> fun)
    {
        final @NonNull AsyncCacheEntry cacheEntry;
        final @NonNull Optional<CacheEntry> entryOpt = tabCompletionCache.get(commandSender);
        if (!entryOpt.isPresent() || !(entryOpt.get() instanceof AsyncCacheEntry))
            cacheEntry = (AsyncCacheEntry) tabCompletionCache.put(commandSender, new AsyncCacheEntry());
        else
            cacheEntry = (AsyncCacheEntry) entryOpt.get();

        final @NonNull Optional<List<String>> suggestions = cacheEntry.suggestionsSubSelection(args.size(), lastArg);
        if (suggestions.isPresent())
            return new Pair<>(suggestions.get(), null);


        final @NonNull CompletableFuture<List<String>> newSuggestions = CompletableFuture.supplyAsync(fun);
        cacheEntry.prepare(newSuggestions, args.size(), lastArg);

        return new Pair<>(null, newSuggestions);
    }

    /**
     * Represents a cached list of tab completion options for an {@link ICommandSender}.
     *
     * @author Pim
     */
    private static class CacheEntry
    {
        /**
         * The number of characters to keep in the cache behind the new one.
         * <p>
         * For example, when set to a value of two and a "lastarg" of "pim", the suggestions list will contain all
         * suggestions for "pi" and "pim", but not "p".
         */
        protected static final int CUTOFF_DELTA = 2;

        /**
         * The cached list of suggestions.
         */
        protected @Nullable List<String> suggestions = null;

        /**
         * The cached last argument.
         */
        protected @NonNull String previousArg = "";

        /**
         * The number of arguments in the command.
         */
        protected int argCount = 0;

        /**
         * Updates the current suggestions data.
         *
         * @param suggestions The updated list of suggestions.
         * @param argCount    The updated number of arguments in the command.
         */
        public void reset(final @NonNull List<String> suggestions, final int argCount, final @NonNull String lastArg)
        {
            this.suggestions = new ArrayList<>(suggestions);
            this.argCount = argCount;
            previousArg = lastArg;
        }

        /**
         * Gets all the cached suggestions
         *
         * @param newArgCount The new number of arguments.
         * @return The list of the narrowed-down suggestions list.
         */
        public @NonNull Optional<List<String>> suggestionsSubSelection(final int newArgCount,
                                                                       final @NonNull String lastArg)
        {
            if (suggestions == null || newArgCount != argCount)
                return Optional.empty();

            argCount = newArgCount;

            // Get the cutoff for the old argument. This is the base string for every entry in the cached list.
            // So, if the provided lastArg does not start with that, we know that we don't have its results cached.
            // Because the CUTOFF_DELTA is 2, we'd get an empty string if there are only 2 characters. Therefore, we
            // try to get the first character in that case (if long enough).
            final int previousCutoff = Math.min(previousArg.length(), Math.max(1, previousArg.length() - CUTOFF_DELTA));
            final @NonNull String basePreviousArg =
                previousArg.substring(0, Math.min(previousArg.length(), previousCutoff));

            // If the basePrevious arg is empty we don't have any data about what substring the argument starts with.
            // So we treat it as an invalid start.
            if (basePreviousArg.isEmpty() || !lastArg.startsWith(basePreviousArg))
                return Optional.empty();

            final int newCutoff = Math.max(0, lastArg.length() - CUTOFF_DELTA);
            suggestions.removeIf(val -> val.length() < newCutoff);

            final @NonNull ArrayList<String> newSuggestions = new ArrayList<>(suggestions.size());
            suggestions.forEach(
                val ->
                {
                    if (val.startsWith(lastArg))
                        newSuggestions.add(val);
                });

            newSuggestions.trimToSize();
            previousArg = lastArg;
            return Optional.of(newSuggestions);
        }
    }

    /**
     * Represents a specialization of the {@link CacheEntry} to use values that may or may not be immediately
     * available.
     *
     * @author Pim
     */
    private static class AsyncCacheEntry extends CacheEntry
    {
        protected @NonNull ENTRY_STATUS entryStatus = ENTRY_STATUS.NULL;

        public void prepare(final @NonNull CompletableFuture<List<String>> newSuggestions, final int argCount,
                            final @NonNull String lastArg)
        {
            entryStatus = ENTRY_STATUS.PENDING;
            newSuggestions.whenComplete((suggestions, throwable) -> reset(suggestions, argCount, lastArg));
        }

        @Override
        public void reset(final @NonNull List<String> suggestions, final int argCount, final @NonNull String lastArg)
        {
            entryStatus = ENTRY_STATUS.AVAILABLE;
            super.reset(suggestions, argCount, lastArg);
        }

        @Override
        public @NonNull Optional<List<String>> suggestionsSubSelection(final int newArgCount,
                                                                       final @NonNull String lastArg)
        {
            // If the data isn't available yet, return an empty list (not an empty optional).
            if (entryStatus == ENTRY_STATUS.PENDING)
                return Optional.of(new ArrayList<>(0));

            return super.suggestionsSubSelection(newArgCount, lastArg);
        }
    }

    private enum ENTRY_STATUS
    {
        NULL,
        PENDING,
        AVAILABLE
    }
}
