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

import lombok.NonNull;
import nl.pim16aap2.cap.SpigotCAP;
import nl.pim16aap2.cap.text.ColorScheme;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Class used to create new {@link ICommandSender} objects for the Spigot platform.
 *
 * @author Pim
 */
public class SpigotCommandSenderFactory
{
    private static final @NonNull ColorScheme EMPTY_COLOR_SCHEME = ColorScheme.builder().build();

    private @NonNull LocaleCache localeCache = new LocaleCache();

    /**
     * Updates the {@link Locale} for a given {@link CommandSender}.
     * <p>
     * Note that this will not directly update the locale of top-level commands for the command sender. If you want to
     * do that as well, use {@link SpigotCAP#updateLocale(CommandSender, Locale)} instead.
     *
     * @param commandSender The {@link CommandSender} for which to update their locale.
     * @param locale        The {@link Locale} to use for the {@link CommandSender}.
     */
    public void updateLocale(final @Nullable CommandSender commandSender, final @Nullable Locale locale)
    {
        localeCache.put(commandSender, locale);
    }

    /**
     * Gets the {@link Locale} that is supposed to be used by a {@link CommandSender}.
     * <p>
     * When the {@link Locale} is null, the default one will be used.
     *
     * @param commandSender The {@link CommandSender} for which to get the {@link Locale}.
     * @return The {@link Locale} to use.
     */
    public @Nullable Locale getLocale(final @NonNull CommandSender commandSender)
    {
        return localeCache.getLocale(commandSender);
    }

    /**
     * Wraps a {@link CommandSender} with an {@link ICommandSender} as used by CAP.
     * <p>
     * This method uses an empty {@link ColorScheme}.
     *
     * @param commandSender The {@link CommandSender} to wrap.
     * @return A {@link ICommandSender} that can be used by CAP.
     */
    public @NonNull ICommandSender wrapCommandSender(final @NonNull CommandSender commandSender)
    {
        return wrapCommandSender(commandSender, null);
    }

    /**
     * Wraps a {@link CommandSender} with an {@link ICommandSender} as used by CAP.
     *
     * @param commandSender The {@link CommandSender} to wrap.
     * @param colorScheme   The {@link ColorScheme} to use for generating messages.
     *                      <p>
     *                      Note that this only applies to players!
     * @return A {@link ICommandSender} that can be used by CAP.
     */
    public @NonNull ICommandSender wrapCommandSender(final @NonNull CommandSender commandSender,
                                                     @Nullable ColorScheme colorScheme)
    {
        colorScheme = colorScheme == null ? EMPTY_COLOR_SCHEME : colorScheme;

        if (commandSender instanceof Player)
            return new SpigotPlayerCommandSender((Player) commandSender, colorScheme,
                                                 localeCache.getLocale(commandSender));
        return new SpigotServerCommandSender(localeCache.getLocale(commandSender));
    }

    /**
     * Updates the {@link ILocaleProvider} provider to use for looking up {@link Locale}s for {@link CommandSender}s.
     *
     * @param localeProvider The new {@link ILocaleProvider}. When null, all {@link CommandSender}s will use the default
     *                       locale.
     */
    public void setLocaleProvider(final @Nullable ILocaleProvider localeProvider)
    {
        localeCache.setLocaleProvider(localeProvider);
    }
}
