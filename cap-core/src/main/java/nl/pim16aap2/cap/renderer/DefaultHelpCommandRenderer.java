package nl.pim16aap2.cap.renderer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import nl.pim16aap2.cap.text.TextType;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents the default implementation of {@link IHelpCommandRenderer}.
 *
 * @author Pim
 */
@Builder(toBuilder = true)
@Getter
public class DefaultHelpCommandRenderer implements IHelpCommandRenderer
{
    // TODO: Make this configurable
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
     * The string to prepend before every summary. Default = "  ".
     */
    @Builder.Default
    protected String summaryIndent = "  ";

    /**
     * Whether or not to display the arguments of each command on the list page.
     */
    @Builder.Default
    protected boolean displayArgumentsForSimple = false;

    protected @NonNull IArgumentRenderer argumentRenderer;

    /**
     * Whether to start counting pages at 0 (<it>false</it>) or 1 (<it>true</it>). Default = true.
     */
    @Builder.Default
    protected boolean startAt1 = true;

    protected DefaultHelpCommandRenderer(final int pageSize, final int firstPageSize, final boolean displayHeader,
                                         final @NonNull String summaryIndent, final boolean displayArgumentsForSimple,
                                         final @Nullable IArgumentRenderer argumentRenderer, final boolean startAt1)
    {
        this.pageSize = pageSize;
        this.firstPageSize = firstPageSize;
        this.displayHeader = displayHeader;
        this.displayArgumentsForSimple = displayArgumentsForSimple;
        this.argumentRenderer = argumentRenderer == null ? DefaultArgumentRenderer.getDefault() : argumentRenderer;
        this.summaryIndent = summaryIndent;
        this.startAt1 = startAt1;
    }

    @Override
    public int getPageOffset()
    {
        return startAt1 ? 1 : 0;
    }

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

    /**
     * Recursively calculates the total number of (sub){@link Command}s to put on the help menu.
     *
     * @param command       The {@link Command} for which to count the number of sub{@link Command}s (including
     *                      itself).
     * @param commandSender The {@link ICommandSender} used for permission checking. See {@link
     *                      ICommandSender#hasPermission(Command)}.
     * @return The total number of {@link Command}s that can be put in a help menu for the given {@link ICommandSender}.
     */
    protected final int getCommandCount(final @NonNull Command command, final @NonNull ICommandSender commandSender)
    {
        int count = 0;
        if (!command.isHidden() && commandSender.hasPermission(command))
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
     *                      ICommandSender#hasPermission(Command)}.
     * @return The total number of help pages available for the {@link Command}.
     */
    protected int getPageCount(final @NonNull Command command, final @NonNull ICommandSender commandSender)
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
     * @param text      The {@link Text} instance to add the page count header to.
     * @param page      The current page number.
     * @param pageCount The total number of available pages.
     */
    protected void renderPageCountHeader(final @NonNull Text text, final int page, final int pageCount)
    {
        // TODO: Make this prettier. It needs nice colors and clickable text.
        text.add(String.format("------- Page (%2d / %2d) -------\n",
                               page + getPageOffset(), pageCount + getPageOffset()));
    }

    @Override
    public @NonNull Text render(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                final @NonNull Command command, final int page)
        throws IllegalValueException
    {
        final int pageCount = getPageCount(command, commandSender);
        if (page > pageCount || page < 0)
            throw new IllegalValueException(command, Integer.toString(page), command.getCap().isDebug());

        Text text = new Text(colorScheme);
        renderPageCountHeader(text, page, pageCount);
        if (page == 0)
            return renderFirstPage(commandSender, colorScheme, text, command);

        final int skip = firstPageSize + (page - 1) * pageSize;
        renderCommands(commandSender, colorScheme, text, getBaseSuperCommand(colorScheme, command),
                       command, pageSize, skip);
        return text;
    }

    @Override
    public @NonNull Text render(final @NonNull ICommandSender commandSender, final @NonNull ColorScheme colorScheme,
                                final @NonNull Command command, final @Nullable String val)
        throws IllegalValueException, CommandNotFoundException
    {
        if (val == null)
            return render(commandSender, colorScheme, command, 0);

        final OptionalInt pageOpt = Util.parseInt(val);
        if (pageOpt.isPresent())
        // Subtract 1 from the desired page if counting starts at 1, because if the user specified page '1',
        // They should receive page '0' instead.
        {
            System.out.println("Selected page: " + pageOpt.getAsInt() + ", startAt1: " + startAt1 + ", final page: " +
                                   (pageOpt.getAsInt() - getPageOffset()));
            return render(commandSender, colorScheme, command, pageOpt.getAsInt() - getPageOffset());
        }

        final @NonNull Optional<Command> subCommand = command.getCap().getCommand(val);
        if (!subCommand.isPresent())
            throw new CommandNotFoundException(val, command.getCap().isDebug());

        return renderLongCommand(commandSender, colorScheme, subCommand.get());
    }

    @Override
    public @NonNull Text renderLongCommand(final @NonNull ICommandSender commandSender,
                                           final @NonNull ColorScheme colorScheme, final @NonNull Command command)
    {
        if (!commandSender.hasPermission(command))
            return new Text(colorScheme);

        final Text text = getBaseSuperCommand(colorScheme, command).add(command.getName(), TextType.COMMAND);
        renderArgumentsShort(colorScheme, text, command);

        if (!command.getDescription(colorScheme).equals(""))
            text.add("\n").add(summaryIndent).add(command.getDescription(colorScheme), TextType.DESCRIPTION);
        renderArgumentsLong(colorScheme, text, command);
        return text;
    }

    /**
     * Recursively constructs the {@link Text} containing the all super {@link Command}s of a {@link Command}.
     * <p>
     * Note that the {@link Command} that is provided inside the optional is also included if possible, so if this is
     * not desired, use this method with {@link Command#getSuperCommand()}.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the {@link Text}.
     * @param command     The {@link Optional} {@link Command} whose super commands to add to the text. If it has no
     *                    super commands (or isn't {{@link Optional#isPresent()}}), it will only append {@link
     *                    #COMMAND_PREFIX} and the name of this command itself (if possible).
     * @return The {@link Text} with all the super {@link Command}s of the provided {@link Command}.
     */
    protected @NonNull Text getBaseSuperCommand(final @NonNull ColorScheme colorScheme,
                                                final @NonNull Optional<Command> command)
    {
        // Base case
        if (!command.isPresent())
            return new Text(colorScheme).add(COMMAND_PREFIX, TextType.COMMAND);

        return getBaseSuperCommand(colorScheme, command.get().getSuperCommand()).add(command.get().getName()).add(" ");
    }

    /**
     * Recursively constructs the {@link Text} containing the all super {@link Command}s of a {@link Command}.
     * <p>
     * Note that the {@link Command} that is provided will not be included.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the {@link Text}.
     * @param command     The {@link Command} whose super commands to add to the text. If it has no super commands, it
     *                    will only append {@link #COMMAND_PREFIX}.
     * @return The {@link Text} with all the super {@link Command}s of the provided {@link Command}.
     */
    protected @NonNull Text getBaseSuperCommand(final @NonNull ColorScheme colorScheme, final @NonNull Command command)
    {
        return getBaseSuperCommand(colorScheme, command.getSuperCommand());
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
     *                      ICommandSender#hasPermission(Command)}.
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
        if (!commandSender.hasPermission(command))
            return text;

        if (displayHeader && !command.getHeader(colorScheme).equals(""))
            text.add(command.getHeader(colorScheme), TextType.HEADER).add("\n");

        renderCommands(commandSender, colorScheme, text, getBaseSuperCommand(colorScheme, command),
                       command, firstPageSize, 0);

        return text;
    }

    /**
     * Recursively renders the given command as well as all its subcommands.
     *
     * @param commandSender The {@link ICommandSender} that is used to check for permissions. Any (sub){@link Command}s
     *                      they do not have access to are not included. See {@link ICommandSender#hasPermission(Command)}.
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
                                                             final @NonNull Text superCommands,
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
        if (!command.isHidden() || !commandSender.hasPermission(command))
        {
            // Only render the command if it doesn't have to be skipped.
            if (skip > skipped)
                ++skipped;
            else
            {
                renderCommand(colorScheme, text, command, superCommands);
                text.add("\n");
                ++added;
            }
        }

        if (added == count)
            return new Pair<>(added, skipped);

        // The current command has to be appended to the super commands, because the
        // current command is the super command of all its sub commands (by definition).
        final Text newSuperCommands =
            new Text(superCommands).add(command.getName(), TextType.COMMAND).add(" ");

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
     *                      Command#isHidden()}.
     * @param superCommands A {@link Text} with all the appended super commands of the current {@link Command}. This
     *                      will be prepended to the {@link Command}.
     */
    protected void renderCommand(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                 final @NonNull Command command, final @NonNull Text superCommands)
    {
        text.add(superCommands).add(command.getName(), TextType.COMMAND);
        renderArgumentsShort(colorScheme, text, command);

        if (!command.getSummary(colorScheme).equals(""))
            text.add("\n").add(summaryIndent).add(command.getSummary(colorScheme), TextType.SUMMARY);
    }

    /**
     * Renders the arguments in short format, see {@link IArgumentRenderer#render(ColorScheme, Argument)}.
     * <p>
     * The arguments are separated by spaces.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the {@link Text}.
     * @param text        The {@link Text} to append the rendered {@link Argument}s to.
     * @param command     The {@link Command} for which to render the {@link Argument}s.
     */
    protected void renderArgumentsShort(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                        final @NonNull Command command)
    {
        for (final Argument<?> argument : command.getArgumentManager().getArguments())
            text.add(" ").add(argumentRenderer.render(colorScheme, argument));
    }

    /**
     * Renders the arguments in short format, see {@link IArgumentRenderer#renderLongFormat(ColorScheme, Argument,
     * String)}.
     * <p>
     * The arguments (and their summaries) are separated by newlines.
     *
     * @param colorScheme The {@link ColorScheme} to use to render the {@link Text}.
     * @param text        The {@link Text} to append the rendered {@link Argument}s to.
     * @param command     The {@link Command} for which to render the {@link Argument}s.
     */
    protected void renderArgumentsLong(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                       final @NonNull Command command)
    {
        for (final Argument<?> argument : command.getArgumentManager().getArguments())
            text.add("\n").add(argumentRenderer.renderLongFormat(colorScheme, argument, summaryIndent));
    }
}
