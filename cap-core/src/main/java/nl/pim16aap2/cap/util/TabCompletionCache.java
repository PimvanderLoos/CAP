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
 * Represents a cache for tabcompletion suggestions. Once a list of suggestions is created for an {@link
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
     * @param fun           The function to retrieve the list of arguments if they cannot be retrieved from cache.
     * @return The list of suggested tab completions.
     *
     * @throws EOFException If the command contains unmatched quotation marks. E.g. '<i>--player="pim 16aap2</i>'.
     */
    public @NonNull List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                       final @NonNull List<String> args,
                                                       final @NonNull CheckedSupplier<List<String>, EOFException> fun)
        throws EOFException
    {
        final @NonNull Optional<CacheEntry> entryOpt = tabCompletionCache.get(commandSender);
        final @NonNull CacheEntry entry;
        if (entryOpt.isPresent())
        {
            entry = entryOpt.get();
            final @NonNull Optional<List<String>> suggestions = entry.suggestionsSubSelection(args);
            if (suggestions.isPresent())
                return suggestions.get();
        }
        else
            entry = tabCompletionCache.put(commandSender, new CacheEntry());

        final @NonNull List<String> newSuggestions = fun.get();
        entry.update(newSuggestions, args.size(), args.get(args.size() - 1));
        return newSuggestions;
    }

    /**
     * Represents a cached list of tab completion options for an {@link ICommandSender}.
     *
     * @author Pim
     */
    private static class CacheEntry
    {
        private @Nullable List<String> suggestions = null;
        private int argCount = 0;
        private @NonNull String lastArg = "";

        /**
         * Updates the current suggestions data.
         *
         * @param suggestions The updated list of suggestions.
         * @param argCount    The updated number of arguments in the command.
         * @param lastArg     The updated value of the last argument in the command.
         */
        public void update(final @NonNull List<String> suggestions, final int argCount, final @NonNull String lastArg)
        {
            this.suggestions = new ArrayList<>(suggestions);
            this.argCount = argCount;
            this.lastArg = lastArg;
        }

        /**
         * Gets all the cached suggestions
         *
         * @param args The list of space-split arguments.
         * @return The list of the narrowed-down suggestions list.
         */
        public @NonNull Optional<List<String>> suggestionsSubSelection(final @NonNull List<String> args)
        {
            if (suggestions == null || args.size() != argCount)
                return Optional.empty();

            argCount = args.size();

            final @NonNull String arg = args.get(args.size() - 1);
            if (!arg.startsWith(lastArg))
                return Optional.empty();

            suggestions.removeIf(val -> !val.startsWith(arg));
            lastArg = arg;

            return Optional.of(new ArrayList<>(suggestions));
        }
    }
}
