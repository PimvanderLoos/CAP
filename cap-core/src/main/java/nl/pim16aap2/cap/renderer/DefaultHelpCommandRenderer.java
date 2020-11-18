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

package nl.pim16aap2.cap.renderer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.localization.Localizer;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents the default implementation of {@link IHelpCommandRenderer}.
 *
 * @author Pim
 */
@SuperBuilder(toBuilder = true)
@Getter
public class DefaultHelpCommandRenderer implements IHelpCommandRenderer
{
    protected static final @NonNull String DEFAULT_PAGE_NAME = "Click me for more information!";
    protected static final @NonNull String DEFAULT_PAGE_NAME_LOCALIZED = "default.helpCommand.page";

    protected static final @NonNull IArgumentRenderer DEFAULT_ARGUMENT_RENDERER = DefaultArgumentRenderer.getDefault();

    // TODO: Make this configurable?
    protected static final String COMMAND_PREFIX = "/";

    /**
     * The number of subcommands to display per page. Default = 5.
     */
    @Builder.Default
    protected int pageSize = 5;

    /**
     * The number of subcommands on the first page. Default = 1.
     */
    @Builder.Default
    protected int firstPageSize = 1;

    /**
     * Whether or not to display the header of the command on the first page. Default = true.
     */
    @Builder.Default
    protected boolean displayHeader = true;

    /**
     * The string to prepend before every description. Default = "  ".
     */
    @Builder.Default
    protected String descriptionIndent = "  ";

    /**
     * Whether or not to display the arguments of each command on the list page. Default = False.
     */
    @Builder.Default
    protected boolean displayArgumentsForSimple = false;

    /**
     * The {@link IArgumentRenderer} renderer that will be used to render arguments.
     */
    @Builder.Default
    protected @NonNull IArgumentRenderer argumentRenderer = DEFAULT_ARGUMENT_RENDERER;

    /**
     * The entry to use for the "page" value in the header. I.e. "--- Page (2 / 5) ---".
     * <p>
     * When left null, this value will either be {@link #DEFAULT_PAGE_NAME} or {@link #DEFAULT_PAGE_NAME_LOCALIZED}
     * depending on whether or not localization is enabled in the provided {@link CAP} instance. See {@link
     * CAP#localizationEnabled()}.
     */
    @Builder.Default
    protected final @Nullable String pageName = null;

    /**
     * Gets a new instance of this {@link DefaultHelpCommandRenderer} using the default values.
     * <p>
     * Use {@link DefaultHelpCommandRenderer#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link DefaultHelpCommandRenderer}.
     */
    public static @NonNull DefaultHelpCommandRenderer getDefault()
    {
        return DefaultHelpCommandRenderer.builder().build();
    }

    protected final @NonNull String getLocalizedMessage(final @NonNull CAP cap,
                                                        final @NonNull ICommandSender commandSender,
                                                        final @Nullable String key,
                                                        final @NonNull String keyFallBack,
                                                        final @NonNull String keyFallBackLocalized)
    {
        if (!cap.localizationEnabled())
            return Util.valOrDefault(key, keyFallBack);
        return cap.getLocalizer().getMessage(Util.valOrDefault(key, keyFallBackLocalized), commandSender.getLocale());
    }

    /**
     * Recursively calculates the total number of (sub){@link Command}s to put on the help menu.
     *
     * @param command       The {@link Command} for which to count the number of sub{@link Command}s (including
     *                      itself).
     * @param commandSender The {@link ICommandSender} used for permission checking. See {@link
     *                      Command#hasPermission(ICommandSender)}.
     * @return The total number of {@link Command}s that can be put in a help menu for the given {@link ICommandSender}.
     */
    protected final int getCommandCount(final @NonNull Command command, final @NonNull ICommandSender commandSender)
    {
        int count = 0;
        if (!command.isVirtual() && command.hasPermission(commandSender))
            ++count;

        for (final @NonNull Command subCommand : command.getSubCommands())
            count += getCommandCount(subCommand, commandSender);
        return count;
    }

    /**
     * Gets the total number of help pages available for a given {@link Command}.
     *
     * @param command       The {@link Command} for which to check the number of help pages.
     * @param commandSender The {@link ICommandSender} used for permission checking. See {@link
     *                      Command#hasPermission(ICommandSender)}.
     * @return The total number of help pages available for the {@link Command}.
     */
    public int getPageCount(final @NonNull Command command, final @NonNull ICommandSender commandSender)
    {
        final int commandCount = getCommandCount(command, commandSender);

        // Get the number of pages that can be filled using the provided number of commands
        // and the provided page size, excluding the number of commands put on the first page.
        return (int) Math.ceil((commandCount - firstPageSize) / (float) pageSize);
    }

    /**
     * Renders the page count header that shows the current page you're viewing and the total number of available
     * pages.
     *
     * @param commandSender The {@link ICommandSender} for which to render the page count header.
     * @param text          The {@link Text} instance to add the page count header to.
     * @param page          The current page number.
     * @param pageCount     The total number of available pages.
     * @param command       The command for which to render the page count header.
     */
    protected void renderPageCountHeader(final @NonNull ICommandSender commandSender, final @NonNull Text text,
                                         final int page, final int pageCount, final @NonNull Command command)
    {
        if (page == 1)
            text.add("--", TextType.REGULAR_TEXT);
        else
            text.add("<<", TextType.COMMAND);

        final @NonNull String localizedPageName = getLocalizedMessage(command.getCap(), commandSender, pageName,
                                                                      DEFAULT_PAGE_NAME, DEFAULT_PAGE_NAME_LOCALIZED);
        text.add(String.format("----- %s (%2d / %2d) -----",
                               localizedPageName, page, pageCount), TextType.REGULAR_TEXT);

        if (page == pageCount)
            text.add("--", TextType.REGULAR_TEXT);
        else
            text.add(">>", TextType.COMMAND);

        text.add("\n");
    }

    @Override
    public @NonNull Text renderOverviewPage(final @NonNull ICommandSender commandSender,
                                            final @NonNull ColorScheme colorScheme,
                                            final @NonNull Command command, final int page)
        throws ValidationFailureException
    {
        final int pageCount = getPageCount(command, commandSender);
        if (page > pageCount || page < 1)
        {
            final @NonNull String localizedMessage = MessageFormat
                .format(command.getCap().getLocalizer().getMessage("error.validation.range", commandSender),
                        page, 1, pageCount);
            throw new ValidationFailureException(Integer.toString(page), localizedMessage, command.getCap().isDebug());
        }

        final @NonNull Text text = new Text(colorScheme);
        renderPageCountHeader(commandSender, text, page, pageCount, command);
        if (page == 1)
            return renderFirstPage(commandSender, colorScheme, text, command);

        // Subtract 2, because we start counting at 1 and because we want to know
        // how many commands were printed up to the previous page.
        final int skip = firstPageSize + (page - 2) * pageSize;
        renderCommands(commandSender, colorScheme, text, getBaseSuperCommand(command, commandSender.getLocale()),
                       command, pageSize, skip);
        return text;
    }

    @Override
    public @NonNull Text render(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                final @NonNull Command command, final @Nullable String val)
        throws ValidationFailureException, CommandNotFoundException
    {
        if (val == null)
            return renderOverviewPage(commandSender, colorScheme, command, 1);

        final @NonNull OptionalInt pageOpt = Util.parseInt(val);
        if (pageOpt.isPresent())
            return renderOverviewPage(commandSender, colorScheme, command, pageOpt.getAsInt() - 1);

        final @NonNull Optional<Command> subCommand = command.getCap().getCommand(val, commandSender.getLocale());
        if (!subCommand.isPresent())
        {
            final @NonNull String localizedMessage =
                MessageFormat.format(command.getCap().getLocalizer().getMessage("error.exception.commandNotFound",
                                                                                commandSender), val);
            throw new CommandNotFoundException(val, localizedMessage, command.getCap().isDebug());
        }

        return renderHelpMenu(commandSender, colorScheme, subCommand.get());
    }

    /**
     * Adds the header for the long help menu of a {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} that is used for localization.
     * @param command       The {@link Command} for which to render the long help menu header.
     * @param text          The {@link Text} instance to add it to.
     */
    protected void renderHelpHeader(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                                    final @NonNull Text text)
    {
        text.add("\n--- " + command.getSectionTitle(commandSender) + " ---\n", TextType.SECTION);
    }

    @Override
    public @NonNull Text renderHelpMenu(final @NonNull ICommandSender commandSender,
                                        final @NonNull ColorScheme colorScheme, final @NonNull Command command)
    {
        if (!command.hasPermission(commandSender))
            return new Text(colorScheme);

        final @NonNull Text text = new Text(colorScheme);
        renderHelpHeader(commandSender, command, text);
        text.add(getBaseSuperCommand(command, commandSender.getLocale()) +
                     command.getName(commandSender.getLocale()), TextType.COMMAND);
        renderArgumentsShort(commandSender.getLocale(), colorScheme, text, command);

        if (!command.getDescription(commandSender).equals(""))
            text.add("\n").add(descriptionIndent).add(command.getDescription(commandSender), TextType.DESCRIPTION);
        renderArgumentsLong(commandSender.getLocale(), colorScheme, text, command);
        return text;
    }

    /**
     * Recursively constructs the String containing the all super {@link Command}s of a {@link Command}.
     * <p>
     * Note that the {@link Command} that is provided inside the optional is also included if possible, so if this is
     * not desired, use this method with {@link Command#getSuperCommand()}.
     *
     * @param command The {@link Optional} {@link Command} whose super commands to add to the text. If it has no super
     *                commands (or isn't {{@link Optional#isPresent()}}), it will only append {@link #COMMAND_PREFIX}
     *                and the name of this command itself (if possible).
     * @param locale  The {@link Locale} to use for rendering the {@link Command}.
     * @return The String with all the super {@link Command}s of the provided {@link Command}.
     */
    protected @NonNull String getBaseSuperCommand(final @NonNull Optional<Command> command,
                                                  final @Nullable Locale locale)
    {
        return command.map(
            value -> getBaseSuperCommand(value.getSuperCommand(), locale) + value.getName(locale) + " "
        ).orElse(COMMAND_PREFIX);
    }

    /**
     * Recursively constructs the String containing the all super {@link Command}s of a {@link Command}.
     * <p>
     * Note that the {@link Command} that is provided will not be included.
     *
     * @param command The {@link Command} whose super commands to add to the text. If it has no super commands, it will
     *                only append {@link #COMMAND_PREFIX}.
     * @param locale  The {@link Locale} to use for rendering the {@link Command}.
     * @return The String with all the super {@link Command}s of the provided {@link Command}.
     */
    protected @NonNull String getBaseSuperCommand(final @NonNull Command command, final @Nullable Locale locale)
    {
        return getBaseSuperCommand(command.getSuperCommand(), locale);
    }

    @Override
    public @NonNull Text renderFirstPage(final @NonNull ICommandSender commandSender,
                                         final @NonNull ColorScheme colorScheme, final @NonNull Command command)
    {
        return renderFirstPage(commandSender, colorScheme, new Text(colorScheme), command);
    }

    /**
     * Renders the first page of the help menu.
     *
     * @param commandSender The {@link ICommandSender} used for permission checking. See {@link
     *                      Command#hasPermission(ICommandSender)}.
     * @param colorScheme   The {@link ColorScheme} to use to render the {@link Text}.
     * @param text          The {@link Text} instance to add the page count header to.
     * @param command       The {@link Command} for which to render the first help page.
     * @return The same {@link Text} instance provided as a parameter, but with the first page appended to it (if
     * possible).
     */
    protected @NonNull Text renderFirstPage(final @NonNull ICommandSender commandSender,
                                            final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                            final @NonNull Command command)
    {
        if (!command.hasPermission(commandSender))
            return text;

        if (displayHeader && !command.getHeader(commandSender).equals(""))
            text.add(command.getHeader(commandSender), TextType.HEADER).add("\n");

        renderCommands(commandSender, colorScheme, text, getBaseSuperCommand(command, null), command, firstPageSize, 0);

        return text;
    }

    /**
     * Recursively renders the given command as well as all its subcommands.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link Command#hasPermission(ICommandSender)}.
     * @param text          The {@link Text} to append the help to.
     * @param superCommands A {@link Text} with all the appended super commands of the current command. This will be
     *                      prepended to the command.
     * @param command       The {@link Command} and {@link Command#getSubCommands()} to render (recursively).
     * @param count         The number of {@link Command}s to render.
     * @param skip          The number of items to skip.
     * @return The number of commands that were added to the {@link Text}.
     */
    protected @NonNull Pair<Integer, Integer> renderCommands(final @NonNull ICommandSender commandSender,
                                                             final @NonNull ColorScheme colorScheme,
                                                             final @NonNull Text text,
                                                             final @NonNull String superCommands,
                                                             final @NonNull Command command, final int count,
                                                             final int skip)
    {
        // Added contains the number of commands added to the text.
        int added = 0;
        if (count < 1)
            return new Pair<>(added, 0);

        // Skipped contains the number of commands that were not rendered because they fell into the skipped category.
        int skipped = 0;

        // Don't render hidden commands, because they're... Well... hidden.
        if (!command.isVirtual() || !command.hasPermission(commandSender))
        {
            // Only render the command if it doesn't have to be skipped.
            if (skip > skipped)
                ++skipped;
            else
            {
                renderCommand(commandSender, colorScheme, text, command, superCommands);
                text.add("\n");
                ++added;
            }
        }

        if (added == count)
            return new Pair<>(added, skipped);

        // The current command has to be appended to the super commands, because the
        // current command is the super command of all its sub commands (by definition).
        final String newSuperCommands = superCommands + command.getName(null) + " ";

        for (final Command subCommand : command.getSubCommands())
        {
            final @NonNull Pair<Integer, Integer> renderResult =
                renderCommands(commandSender, colorScheme, text, newSuperCommands, subCommand,
                               count - added, skip - skipped);

            added += renderResult.first;
            skipped += renderResult.second;

            if (added >= count)
                break;
        }
        return new Pair<>(added, skipped);
    }

    /**
     * Renders a command and appends it to the provided {@link Text}.
     *
     * @param text          The {@link Text} to append the rendered {@link Command} to.
     * @param command       The {@link Command} to render. Note that this method does not care about {@link
     *                      Command#isVirtual()}.
     * @param superCommands A {@link Text} with all the appended super commands of the current {@link Command}. This
     *                      will be prepended to the {@link Command}.
     */
    protected void renderCommand(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                 final @NonNull Text text, final @NonNull Command command,
                                 final @NonNull String superCommands)
    {
        text.add(superCommands + command.getName(commandSender.getLocale()), TextType.COMMAND);
        renderArgumentsShort(commandSender.getLocale(), colorScheme, text, command);

        if (!command.getSummary(commandSender).equals(""))
            text.add("\n").add(descriptionIndent).add(command.getSummary(commandSender), TextType.SUMMARY);
    }

    /**
     * Renders the arguments in short format, see {@link IArgumentRenderer#render(Localizer, Locale, ColorScheme,
     * Argument)}.
     * <p>
     * The arguments are separated by spaces.
     *
     * @param locale      The {@link Locale} to use for rendering the {@link Command}.
     * @param colorScheme The {@link ColorScheme} to use to render the {@link Text}.
     * @param text        The {@link Text} to append the rendered {@link Argument}s to.
     * @param command     The {@link Command} for which to render the {@link Argument}s.
     */
    protected void renderArgumentsShort(final @Nullable Locale locale, final @NonNull ColorScheme colorScheme,
                                        final @NonNull Text text, final @NonNull Command command)
    {
        for (final Argument<?> argument : command.getArgumentManager().getArguments())
            text.add(" ").add(argumentRenderer.render(command.getCap().getLocalizer(), locale, colorScheme, argument));
    }

    /**
     * Renders the arguments in short format, see {@link IArgumentRenderer#renderLongFormat(Localizer, Locale,
     * ColorScheme, Argument, String)}.
     * <p>
     * The arguments (and their summaries) are separated by newlines.
     *
     * @param locale      The {@link Locale} to use for rendering the {@link Command}.
     * @param colorScheme The {@link ColorScheme} to use to render the {@link Text}.
     * @param text        The {@link Text} to append the rendered {@link Argument}s to.
     * @param command     The {@link Command} for which to render the {@link Argument}s.
     */
    protected void renderArgumentsLong(final @Nullable Locale locale, final @NonNull ColorScheme colorScheme,
                                       final @NonNull Text text, final @NonNull Command command)
    {
        for (final Argument<?> argument : command.getArgumentManager().getArguments())
            text.add("\n")
                .add(argumentRenderer
                         .renderLongFormat(command.getCap().getLocalizer(), locale, colorScheme,
                                           argument, descriptionIndent));
    }
}
