package nl.pim16aap2.cap.util;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.command.Command;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents a map of values with Strings as keys for several {@link Locale}s.
 *
 * @param <T> The type of the values to store.
 * @author Pim
 */
public abstract class LocalizedMap<T>
{
    /**
     * The {@link CAP} instance managing this object.
     */
    protected final @NonNull CAP cap;

    /**
     * The map containing all registered items, with their names as key.
     */
    private final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull T>> localizedMap = new LinkedHashMap<>();

    protected LocalizedMap(final @NonNull CAP cap, final int initialCapacity)
    {
        this.cap = cap;

        for (final @NonNull Locale locale : cap.getLocales())
            localizedMap.put(locale, new HashMap<>(initialCapacity));
    }

    protected LocalizedMap(final @NonNull CAP cap)
    {
        this(cap, 16); // 16 is the default HashMap size.
    }

    /**
     * Adds the entry for every locale.
     *
     * @param key         The key of the entry to register to register.
     * @param caseChecker The function to use to make sure the case is correct. (e.g. {@link
     *                    CAP#getCommandNameCaseCheck(String)}.
     */
    protected void addEntry(final @NonNull String key, final @NonNull T value,
                            final @NonNull Function<@NonNull String, @NonNull String> caseChecker)
    {
        for (final @NonNull Locale locale : cap.getLocales())
        {
            final @NonNull String name = caseChecker.apply(cap.getMessage(key, locale));
            localizedMap.get(locale).put(name, value);
        }
    }

    /**
     * Gets a {@link Command} from its name.
     *
     * @param key         The key to search for.
     * @param locale      The {@link Locale} for which to get the {@link Command}.
     * @param caseChecker The function to use to make sure the case is correct. (e.g. {@link
     *                    CAP#getCommandNameCaseCheck(String)}.
     * @return The entry associated with the provided key in the given locale, if it could be found.
     */
    protected @NonNull Optional<T> getEntry(@Nullable String key, @Nullable Locale locale,
                                            final @NonNull Function<@NonNull String, @NonNull String> caseChecker)
    {
        locale = Util.valOrDefault(locale, cap.getDefaultLocale());
        if (key == null)
            return Optional.empty();

        key = cap.getMessage(key, locale);

        final @NonNull Optional<T> cmd = getFromMap(localizedMap, locale, caseChecker.apply(key));
        return cmd;
    }

    /**
     * Gets an entry from a localized map.
     *
     * @param map    The {@link Map} to retrieve the entry from.
     * @param locale The {@link Locale} to search in.
     * @param key    The key to search for in the locale.
     * @return The value if it could be found.
     */
    private @NonNull Optional<T> getFromMap(final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull T>> map,
                                            final Locale locale, final @NonNull String key)
    {
        return Optional.ofNullable(map.get(locale)).map(entry -> entry.get(key));
    }

    public @NonNull Map<@NonNull String, @NonNull T> get()
    {
        return get(null);
    }

    public @NonNull Map<@NonNull String, @NonNull T> get(final @Nullable Locale locale)
    {
        final @NonNull Map<@NonNull String, @NonNull T> map = localizedMap.get(Util.valOrDefault(locale,
                                                                                                 cap.getDefaultLocale()));
        return map;
    }
}
