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
        final @NonNull Optional<CacheEntry> entryOpt = tabCompletionCache.get(commandSender);
        final @NonNull CacheEntry entry;
        if (entryOpt.isPresent())
        {
            entry = entryOpt.get();
            final @NonNull Optional<List<String>> suggestions = entry.suggestionsSubSelection(args.size(), lastArg);
            if (suggestions.isPresent())
                return suggestions.get();
        }
        else
            entry = tabCompletionCache.put(commandSender, new CacheEntry());

        final @NonNull List<String> newSuggestions = fun.get();
        entry.update(newSuggestions, args.size(), lastArg);
        return newSuggestions;
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
        private static final int CUTOFF_DELTA = 2;

        /**
         * The cached list of suggestions.
         */
        private @Nullable List<String> suggestions = null;

        /**
         * The cached last argument.
         */
        private @NonNull String previousArg = "";

        /**
         * The number of arguments in the command.
         */
        private int argCount = 0;

        /**
         * Updates the current suggestions data.
         *
         * @param suggestions The updated list of suggestions.
         * @param argCount    The updated number of arguments in the command.
         */
        public void update(final @NonNull List<String> suggestions, final int argCount, final @NonNull String lastArg)
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
}
