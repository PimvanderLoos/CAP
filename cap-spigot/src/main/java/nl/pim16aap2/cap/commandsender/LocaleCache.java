package nl.pim16aap2.cap.commandsender;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Represents a cache for the {@link Locale}s used by each {@link ICommandSender}.
 *
 * @author Pim
 */
// TODO: This should be owned by CAP, not the factory. It's not specific enough for that.
@RequiredArgsConstructor
class LocaleCache
{
    private final @NonNull Map<CommandSender, Locale> localeMap = new WeakHashMap<>();

    @Setter
    @Getter
    private @Nullable ILocaleProvider localeProvider;

    /**
     * Gets the {@link Locale} for a {@link CommandSender}.
     *
     * @param commandSender The {@link CommandSender} for which to get the {@link Locale}.
     * @return The {@link Locale} that will be used for this {@link CommandSender}.
     */
    public @Nullable Locale getLocale(final @NonNull CommandSender commandSender)
    {
        // If the localeProvider is null and there aren't any entries in the map,
        // We can be sure that there are no values in the map, nor will there be.
        if (localeProvider == null && localeMap.size() == 0)
            return null;

        return localeProvider == null ?
               localeMap.get(commandSender) :
               localeMap.computeIfAbsent(commandSender, (sender) -> localeProvider.getLocale(sender));
    }

    /**
     * Updates the {@link Locale} for a given {@link CommandSender}.
     *
     * @param commandSender The {@link CommandSender} for which to update their locale.
     * @param locale        The {@link Locale} to use for the {@link CommandSender}.
     */
    public void put(final @Nullable CommandSender commandSender, final @Nullable Locale locale)
    {
        localeMap.put(commandSender, locale);
    }
}
