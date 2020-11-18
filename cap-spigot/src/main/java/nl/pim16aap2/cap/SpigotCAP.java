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

package nl.pim16aap2.cap;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.AllowedCommandSenderType;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.commandsender.ILocaleProvider;
import nl.pim16aap2.cap.commandsender.ISpigotCommandSender;
import nl.pim16aap2.cap.commandsender.SpigotCommandSenderFactory;
import nl.pim16aap2.cap.commandsender.SpigotServerCommandSender;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.renderer.SpigotHelpCommandRenderer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.SpigotColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a specialized class of {@link CAP} for the Spigot platform.
 *
 * @author Pim
 */
public class SpigotCAP extends CAP
{
    // Make sure this package was relocated properly.
    static
    {
        final String defaultPackage = new String(
            new byte[]{'n', 'l', '.', 'p', 'i', 'm', '1', '6', 'a', 'a', 'p', '2', '.', 'c', 'a', 'p'});
        final String examplePackage = new String(
            new byte[]{'n', 'l', '.', 'p', 'i', 'm', '1', '6', 'a', 'a', 'p', '2', '.', 't', 'e', 's', 't', '.',
                       'l', 'i', 'b'});

        if (CAP.class.getPackage().getName().equals(defaultPackage) ||
            CAP.class.getPackage().getName().equals(examplePackage))
            throw new IllegalStateException("CAP was not relocated properly!");
    }

    @Getter
    private final @NonNull JavaPlugin plugin;

    @Getter
    @Setter
    private @NonNull ColorScheme colorScheme;

    @Getter
    private final @NonNull SpigotCommandSenderFactory commandSenderFactory;

    private final @NonNull CommandRegistrator commandRegistrator = new CommandRegistrator();

    /**
     * Contains the names of all top-level-commands for every locale.
     */
    private final @NonNull Set<@NonNull String> topLevelCommandNames = new HashSet<>();

    /**
     * @param helpCommandRenderer           See {@link CAP#helpCommandRenderer}.
     * @param debug                         See {@link CAP#debug}.
     * @param plugin                        The {@link JavaPlugin} that manages this object.
     * @param colorScheme                   The default {@link ColorScheme} to use for players.
     * @param exceptionHandler              See {@link CAP#exceptionHandler}.
     * @param separator                     See {@link CAP#separator}.
     * @param cacheTabCompletionSuggestions See {@link CAP#cacheTabCompletionSuggestions}.
     * @param caseSensitive                 See {@link CAP#caseSensitive}.
     * @param localizer                     See {@link CAP#localizer}.
     * @param commandSenderFactory          The factory for creating {@link ICommandSender}s for the Spigot platform.
     *                                      Defaults to {@link SpigotCommandSenderFactory}.
     * @param localeProvider                The {@link ILocaleProvider}. When null, all {@link CommandSender}s will use
     *                                      the default locale.
     */
    @Builder(builderMethodName = "spigotCAPBuilder")
    protected SpigotCAP(final @Nullable DefaultHelpCommandRenderer helpCommandRenderer, final boolean debug,
                        final @NonNull JavaPlugin plugin, final @Nullable ColorScheme colorScheme,
                        final @Nullable ExceptionHandler exceptionHandler,
                        final @Nullable Character separator, final @Nullable Boolean cacheTabCompletionSuggestions,
                        final boolean caseSensitive,
                        final @Nullable Localizer localizer,
                        final @Nullable SpigotCommandSenderFactory commandSenderFactory,
                        final @Nullable ILocaleProvider localeProvider)
    {
        super(Util.valOrDefault(helpCommandRenderer, SpigotHelpCommandRenderer.getDefault()),
              Util.valOrDefault(cacheTabCompletionSuggestions, true),
              Util.valOrDefault(exceptionHandler, ExceptionHandler.getDefault()),
              Util.valOrDefault(separator, ' '), debug, caseSensitive, localizer);

        this.plugin = plugin;
        this.colorScheme = Util.valOrDefault(colorScheme, getDefaultColorScheme());
        Bukkit.getPluginManager().registerEvents(new CommandListener(this), plugin);

        if (exceptionHandler == null && getExceptionHandler() != null)
            getExceptionHandler().setHandler(NoPermissionException.class, SpigotCAP::handleNoPermissionException);

        this.commandSenderFactory = Util.valOrDefault(commandSenderFactory,
                                                      new SpigotCommandSenderFactory());
        this.commandSenderFactory.setLocaleProvider(localeProvider);

        plugin.getServer().getPluginManager();
    }

    /**
     * Registers all commands in the {@link #topLevelCommandMap} with Spigot so they can be used for tab-completion
     * suggestions.
     * <p>
     * If you do not call this method, all tab-completion suggestions will be broken.
     */
    public void registerTopLevelCommands()
    {
        for (final @NonNull Locale locale : localizer.getLocales())
        {
            final @NonNull Map<@NonNull String, @NonNull Command> localeMap = topLevelCommandMap.getLocaleMap(locale);
            commandRegistrator.registerCommands(plugin, localeMap);
            topLevelCommandNames.addAll(localeMap.keySet());
        }
    }

    /**
     * Checks if a top-level-command with the provided name is valid in the provided {@link Locale}.
     * <p>
     * If the provided name cannot be mapped to a top-level-command managed by this instance, it doesn't count as
     * valid.
     * <p>
     * Only top-level-commands that do exist and also in the provided locale are considered valid.
     *
     * @param name   The name of the potential top-level-command to look for.
     * @param locale The {@link Locale} the top-level-command must exist in (if it exists) for it to be valid.
     * @return True if the provided name can be mapped to a top-level-command in the provided locale.
     */
    public @NonNull TopLevelCommandStatus isValidTopLevelCommand(final @Nullable String name,
                                                                 final @Nullable Locale locale)
    {
        if (!topLevelCommandNames.contains(name))
            return TopLevelCommandStatus.UNMAPPED;

        return topLevelCommandMap.getLocaleMap(locale).containsKey(name) ?
               TopLevelCommandStatus.VALID : TopLevelCommandStatus.INVALID_LOCALE;
    }

    /**
     * Gets a new instance of this {@link SpigotCAP} using the default values.
     *
     * @return A new instance of this {@link SpigotCAP}.
     */
    public static @NonNull SpigotCAP getDefault()
    {
        return SpigotCAP.spigotCAPBuilder().build();
    }

    /**
     * Updates the {@link Locale} for an {@link ICommandSender}.
     *
     * @param commandSender The {@link ICommandSender} for which to update the {@link Locale}.
     * @param locale        The new {@link Locale}. When null, the default {@link Locale} will be used.
     */
    public void updateLocale(final @NonNull ICommandSender commandSender, final @Nullable Locale locale)
    {
        // We're on the Spigot platform, so this should never trigger, but just in case.
        if (!(commandSender instanceof ISpigotCommandSender))
            throw new IllegalArgumentException(
                commandSender.getClass().getCanonicalName() + " is not a ISpigotCommandSender!");

        final @NonNull CommandSender spigotCommandSender = ((ISpigotCommandSender) commandSender).getCommandSender();
        updateLocale(spigotCommandSender, locale);
    }

    /**
     * Updates the {@link Locale} for a {@link CommandSender}.
     *
     * @param commandSender The {@link CommandSender} for which to update the {@link Locale}.
     * @param locale        The new {@link Locale}. When null, the default {@link Locale} will be used.
     */
    public void updateLocale(final @NonNull CommandSender commandSender, final @Nullable Locale locale)
    {
        commandSenderFactory.updateLocale(commandSender, locale);
        // Updating the command ensures that the client knows the top-level commands exist.
        if (commandSender instanceof Player)
            ((Player) commandSender).updateCommands();
    }

    /**
     * See {@link CAP#parseInput(ICommandSender, String)}.
     */
    public @NonNull Optional<CommandResult> parseInput(final @NonNull CommandSender sender,
                                                       final @NonNull String message)
    {
        final @NonNull ICommandSender commandSender = commandSenderFactory.wrapCommandSender(sender, colorScheme);
        return parseInput(commandSender, message);
    }

    /**
     * Generates the default {@link SpigotColorScheme}.
     *
     * @return The default {@link SpigotColorScheme}.
     */
    public static @NonNull SpigotColorScheme getDefaultColorScheme()
    {
        return SpigotColorScheme
            .spigotColorSchemeBuilder()
            .addStyle(TextType.COMMAND, ChatColor.GOLD)
            .addStyle(TextType.OPTIONAL_PARAMETER, ChatColor.BLUE)
            .addStyle(TextType.OPTIONAL_PARAMETER_FLAG, ChatColor.LIGHT_PURPLE)
            .addStyle(TextType.OPTIONAL_PARAMETER_SEPARATOR, ChatColor.DARK_RED)
            .addStyle(TextType.OPTIONAL_PARAMETER_LABEL, ChatColor.DARK_AQUA)
            .addStyle(TextType.REQUIRED_PARAMETER, ChatColor.RED)
            .addStyle(TextType.REQUIRED_PARAMETER_FLAG, ChatColor.DARK_BLUE)
            .addStyle(TextType.REQUIRED_PARAMETER_SEPARATOR, ChatColor.BLACK)
            .addStyle(TextType.REQUIRED_PARAMETER_LABEL, ChatColor.WHITE)
            .addStyle(TextType.SUMMARY, ChatColor.AQUA)
            .addStyle(TextType.DESCRIPTION, ChatColor.GREEN)
            .addStyle(TextType.REGULAR_TEXT, ChatColor.DARK_PURPLE)
            .addStyle(TextType.HEADER, ChatColor.GREEN)
            .addStyle(TextType.SECTION, ChatColor.AQUA)
            .addStyle(TextType.FOOTER, ChatColor.DARK_RED)
            .addStyle(TextType.ERROR, ChatColor.DARK_RED, ChatColor.BOLD)
            .build();
    }

    /**
     * Checks if the {@link ICommandSender} is an instance of a {@link SpigotServerCommandSender} and they don't have
     * permission to a command, it means that that command was set to {@link AllowedCommandSenderType#PLAYER_ONLY}.
     * <p>
     * As such, we send a specialized message saying that the given command can only be used by {@link Player}s.
     *
     * @param commandSender The {@link ICommandSender} to check.
     * @param e             The Exception.
     */
    public static void handleNoPermissionException(final @NonNull ICommandSender commandSender,
                                                   final @NonNull NoPermissionException e)
    {
        if (commandSender instanceof SpigotServerCommandSender)
        {
            commandSender.sendMessage(
                new Text(commandSender.getColorScheme()).add("Only players can use this command!", TextType.ERROR));
        }
        else
            ExceptionHandler.handleCAPException(commandSender, e);
    }

    enum TopLevelCommandStatus
    {
        /**
         * The provided string cannot be mapped to a top-level-command.
         */
        UNMAPPED,

        /**
         * A top-level-command was found, but not in the desired locale.
         */
        INVALID_LOCALE,

        /**
         * A top-level-command was found in the desired locale.
         */
        VALID
    }
}
