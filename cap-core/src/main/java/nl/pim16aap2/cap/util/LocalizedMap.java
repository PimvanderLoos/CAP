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
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.localization.Localizer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
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
     * The {@link Localizer} instance to use for localizing messages.
     */
    protected final @NonNull Localizer localizer;

    /**
     * The map containing all registered items, with their names as key.
     */
    private final @NonNull Map<@NonNull Locale, Map<@NonNull String, @NonNull T>> localizedMap = new HashMap<>();

    protected LocalizedMap(final @NonNull Localizer localizer, final int initialCapacity)
    {
        this.localizer = localizer;

        for (final @NonNull Locale locale : localizer.getLocales())
            localizedMap.put(locale, new LinkedHashMap<>(initialCapacity));
    }

    protected LocalizedMap(final @NonNull Localizer localizer)
    {
        this(localizer, 16); // 16 is the default HashMap size.
    }

    /**
     * Removes all entries from every localized map.
     */
    public void clear()
    {
        localizedMap.values().forEach(Map::clear);
    }

    /**
     * Adds the entry for every locale.
     *
     * @param keyFinder   The fun
     * @param value       The value to add for the provided key.
     * @param caseChecker The function to use to make sure the case is correct. (e.g. {@link
     *                    CAP#getCommandNameCaseCheck(String)}.
     */
    protected void addEntry(final @NonNull BiFunction<@NonNull Localizer, @NonNull Locale, @NonNull String> keyFinder,
                            final @NonNull T value,
                            final @NonNull Function<@NonNull String, @NonNull String> caseChecker)
    {
        for (final @NonNull Locale locale : localizer.getLocales())
        {
            final @NonNull String name =
                caseChecker.apply(localizer.getMessage(keyFinder.apply(localizer, locale), locale));

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
        locale = Util.valOrDefault(locale, localizer.getDefaultLocale());
        if (key == null)
            return Optional.empty();

        key = localizer.getMessage(key, locale);

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

    /**
     * Gets the entry map for the default locale. See {@link #getLocaleMap(Locale)}.
     *
     * @return The entry map for the default {@link Locale}
     */
    public @NonNull Map<@NonNull String, @NonNull T> getLocaleMap()
    {
        return getLocaleMap(null);
    }

    /**
     * Gets the entry map for a specific {@link Locale}.
     *
     * @param locale The specific {@link Locale}.
     * @return The entry map for the {@link Locale}
     */
    public @NonNull Map<@NonNull String, @NonNull T> getLocaleMap(final @Nullable Locale locale)
    {
        final @Nullable Map<@NonNull String, @NonNull T> map =
            localizedMap.get(Util.valOrDefault(locale, localizer.getDefaultLocale()));
        if (map == null)
        {
            System.out.printf("Localizer default locale: %s, locales count: %d, localizedMapCount: %d\n",
                              Util.valOrDefault(localizer.getDefaultLocale(), "NULL"),
                              localizer.getLocales().length, localizedMap.size());
            throw new NullPointerException("map is marked non-null but is null");
        }
        return map;
    }
}
