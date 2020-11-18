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
    private final @NonNull Map<@NonNull CommandSender, Locale> localeMap = new WeakHashMap<>();

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
    public void put(final @NonNull CommandSender commandSender, final @Nullable Locale locale)
    {
        localeMap.put(commandSender, locale);
    }
}
