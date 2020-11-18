/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.cap.util;

import lombok.NonNull;
import nl.pim16aap2.cap.commandparser.TabCompletionSuggester;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.util.cache.TimedCache;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
    /**
     * Matches zero or one leading quotation marks in a String.
     */
    private static final Pattern LEADING_QUOTATION_MARK = Pattern.compile("^[\"]?");

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
     * @param openEnded     Whether the cached results are openEnded or not. See {@link TabCompletionSuggester#isOpenEnded()}.
     * @return The list of suggested tab completions.
     */
    public @NonNull List<@NonNull String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                                final @NonNull List<@NonNull String> args,
                                                                final @NonNull String lastArg,
                                                                final @NonNull Supplier<List<@NonNull String>> fun,
                                                                final boolean openEnded)
    {
        final @NonNull CacheEntry cacheEntry = tabCompletionCache.computeIfAbsent(commandSender, k -> new CacheEntry());

        final @NonNull Optional<List<@NonNull String>> suggestions =
            cacheEntry.suggestionsSubSelection(args.size(), lastArg, openEnded, commandSender.getLocale());

        if (suggestions.isPresent())
            return suggestions.get();

        final @NonNull List<@NonNull String> newSuggestions = fun.get();
        cacheEntry.reset(newSuggestions, args.size(), lastArg, openEnded, commandSender.getLocale());
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
     * @param openEnded     Whether the cached results are openEnded or not. See {@link TabCompletionSuggester#isOpenEnded()}.
     * @return The {@link CompletableFuture} of the list of suggested tab completions.
     */
    public @NonNull CompletableFuture<List<@NonNull String>> getTabCompleteOptionsAsync(
        final @NonNull ICommandSender commandSender, final @NonNull List<@NonNull String> args,
        final @NonNull String lastArg, final @NonNull Supplier<List<@NonNull String>> fun, final boolean openEnded)
    {
        final @NonNull Triple<List<@NonNull String>, CompletableFuture<List<@NonNull String>>, @NonNull AsyncCacheEntry> result =
            getAsyncCachedEntrySuggestions(commandSender, args, lastArg, fun, openEnded);

        if (result.first != null)
            return CompletableFuture.completedFuture(result.first);
        return result.second;
    }

    /**
     * Gets the list of suggested tab complete options for an {@link ICommandSender} base on the current arguments.
     * <p>
     * If the results are not cached, the results will be put in the cache using an asynchronous method.
     * <p>
     * Unlike {@link #getTabCompleteOptionsAsync(ICommandSender, List, String, Supplier, boolean)}, this method will not
     * return a {@link CompletableFuture} with the results if they had to be retrieved async, but instead, it will
     * return an empty list.
     * <p>
     * Successive calls will keep returning empty Optionals until the async supplier has supplied the cache with a
     * result. From then on, it will retrieve any values it can find in the cache.
     *
     * @param commandSender The {@link ICommandSender} for which to get the list of suggested tab completions.
     * @param args          The current list of arguments.
     * @param lastArg       The last argument in the command. This may or may not be the last entry in the list of
     *                      arguments, but the parser can figure that out.
     * @param fun           The function to retrieve the {@link CompletableFuture} list of arguments if they cannot be
     *                      retrieved from cache.
     * @param openEnded     Whether the cached results are openEnded or not. See {@link TabCompletionSuggester#isOpenEnded()}.
     * @return The list of suggested tab completions if one could be found. If no results are in the cache yet an empty
     * optional is returned.
     */
    public @NonNull Optional<List<@NonNull String>> getDelayedTabCompleteOptions(
        final @NonNull ICommandSender commandSender, final @NonNull List<@NonNull String> args,
        final @NonNull String lastArg, final @NonNull Supplier<List<@NonNull String>> fun, final boolean openEnded)
    {
        final @NonNull Triple<List<@NonNull String>, CompletableFuture<List<@NonNull String>>, @NonNull AsyncCacheEntry> result =
            getAsyncCachedEntrySuggestions(commandSender, args, lastArg, fun, openEnded);

        // Only return the list if the result
        if (result.first != null && result.third.entryStatus == ENTRY_STATUS.AVAILABLE)
            return Optional.of(result.first);
        return Optional.empty();
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
     * @param openEnded     Whether the cached results are openEnded or not. See {@link TabCompletionSuggester#isOpenEnded()}.
     * @return Either a list of suggestions or a {@link CompletableFuture} with the suggestions. Only a single value is
     * returned and the other value is always null. So if a list of suggestions could be found, those will be returned
     * and null for the future one. If no list of suggestions could be found, the future suggestions will be returned
     * and the list will be null.
     */
    private @NonNull Triple<List<@NonNull String>, CompletableFuture<List<@NonNull String>>, @NonNull AsyncCacheEntry> getAsyncCachedEntrySuggestions(
        final @NonNull ICommandSender commandSender, final @NonNull List<@NonNull String> args,
        final @NonNull String lastArg, final @NonNull Supplier<List<@NonNull String>> fun, final boolean openEnded)
    {
        final @NonNull AsyncCacheEntry cacheEntry =
            (AsyncCacheEntry) tabCompletionCache.compute(commandSender, (key, entry) ->
            {
                if (!(entry instanceof AsyncCacheEntry))
                    return new AsyncCacheEntry();
                return entry;
            });

        final @NonNull Optional<List<@NonNull String>> suggestions =
            cacheEntry.suggestionsSubSelection(args.size(), lastArg, openEnded, commandSender.getLocale());

        if (suggestions.isPresent())
            return new Triple<>(suggestions.get(), null, cacheEntry);

        final @NonNull CompletableFuture<List<@NonNull String>> newSuggestions = CompletableFuture.supplyAsync(fun);
        cacheEntry.prepare(newSuggestions, args.size(), lastArg, openEnded, commandSender.getLocale());

        return new Triple<>(null, newSuggestions, cacheEntry);
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
        protected @Nullable List<@NonNull String> suggestions = null;

        /**
         * The cached last argument.
         */
        protected @NonNull String previousArg = "";

        /**
         * The number of arguments in the command.
         */
        protected int argCount = 0;

        /**
         * Keeps track of whether the current suggestions were built from open-ended input.
         * <p>
         * See {@link TabCompletionSuggester#isOpenEnded()}.
         */
        protected boolean openEnded;

        protected @Nullable Locale locale;

        /**
         * Updates the current suggestions data.
         *
         * @param suggestions The updated list of suggestions.
         * @param argCount    The updated number of arguments in the command.
         * @param lastArg     The last argument in the commandline input.
         * @param openEnded   Whether the cached results are openEnded or not. See {@link TabCompletionSuggester#isOpenEnded()}.
         */
        public void reset(final @NonNull List<@NonNull String> suggestions, final int argCount,
                          final @NonNull String lastArg, final boolean openEnded, final @Nullable Locale locale)
        {
            this.suggestions = new ArrayList<>(suggestions);
            this.argCount = argCount;
            previousArg = lastArg;
            this.openEnded = openEnded;
            this.locale = locale;
        }

        /**
         * Gets all the cached suggestions
         *
         * @param newArgCount The new number of arguments.
         * @param lastArg     The value of the last argument.
         * @param openEnded   Whether the cached results are openEnded or not. See {@link TabCompletionSuggester#isOpenEnded()}.
         * @return The list of the narrowed-down suggestions list.
         */
        public @NonNull Optional<List<@NonNull String>> suggestionsSubSelection(final int newArgCount,
                                                                                final @NonNull String lastArg,
                                                                                final boolean openEnded,
                                                                                final @Nullable Locale locale)
        {
            if (suggestions == null || newArgCount != argCount || openEnded && !this.openEnded ||
                !Objects.equals(this.locale, locale))
            {
                this.openEnded = openEnded;
                this.locale = locale;
                return Optional.empty();
            }
            // When both the cached suggestions were open ended and the current status is also open ended,
            // we can just return the current stuff.
            // At this point this.openEnded has to be true as well.
            else if (openEnded)
            {
                this.locale = locale;
                return Optional.of(new ArrayList<>(suggestions));
            }
            this.openEnded = false;

            argCount = newArgCount;

            // Get the cutoff for the old argument. This is the base string for every entry in the cached list.
            // So, if the provided lastArg does not start with that, we know that we don't have its results cached.
            // Because the CUTOFF_DELTA is 2, we'd get an empty string if there are only 2 characters. Therefore, we
            // try to get the first character in that case (if long enough).
            final int previousCutoff = Math.min(previousArg.length(),
                                                Math.max(1, previousArg.length() - CUTOFF_DELTA));
            final @NonNull String basePreviousArg =
                previousArg.substring(0, Math.min(previousArg.length(), previousCutoff));

            // If the basePrevious arg is empty we don't have any data about what substring the argument starts with.
            // So we treat it as an invalid start.
            if (basePreviousArg.isEmpty() || !lastArg.startsWith(basePreviousArg))
                return Optional.empty();

            // Get rid of all entries that do not meet the cutoff.
            final int newCutoff = Math.max(0, lastArg.length() - CUTOFF_DELTA);
            suggestions.removeIf(val -> val.length() < newCutoff);

            final @NonNull ArrayList<@NonNull String> newSuggestions;

            newSuggestions = new ArrayList<>(suggestions.size());
            final Pattern search = Pattern.compile(LEADING_QUOTATION_MARK + Pattern.quote(lastArg));

            suggestions.forEach(
                val ->
                {
                    if (search.matcher(val).find())
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

        public void prepare(final @NonNull CompletableFuture<List<@NonNull String>> newSuggestions, final int argCount,
                            final @NonNull String lastArg, final boolean openEnded,
                            final @Nullable Locale locale)
        {
            entryStatus = ENTRY_STATUS.PENDING;
            newSuggestions.whenComplete((suggestions, throwable) ->
                                            reset(suggestions, argCount, lastArg, openEnded, locale));
        }

        @Override
        public void reset(final @NonNull List<@NonNull String> suggestions, final int argCount,
                          final @NonNull String lastArg, final boolean openEnded,
                          final @Nullable Locale locale)
        {
            entryStatus = ENTRY_STATUS.AVAILABLE;
            super.reset(suggestions, argCount, lastArg, openEnded, locale);
        }

        @Override
        public @NonNull Optional<List<@NonNull String>> suggestionsSubSelection(final int newArgCount,
                                                                                final @NonNull String lastArg,
                                                                                final boolean openEnded,
                                                                                final @Nullable Locale locale)
        {
            // If the data isn't available yet, return an empty list (not an empty optional).
            if (entryStatus == ENTRY_STATUS.PENDING)
                return Optional.of(new ArrayList<>(0));

            return super.suggestionsSubSelection(newArgCount, lastArg, openEnded, locale);
        }
    }

    private enum ENTRY_STATUS
    {
        NULL,
        PENDING,
        AVAILABLE
    }
}
