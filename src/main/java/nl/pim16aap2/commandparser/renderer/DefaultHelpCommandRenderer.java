package nl.pim16aap2.commandparser.renderer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import nl.pim16aap2.commandparser.text.TextType;
import nl.pim16aap2.commandparser.util.Pair;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * Represents the default {@link IHelpCommandRenderer}.
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
        this.argumentRenderer = argumentRenderer == null ? new DefaultArgumentRenderer() : argumentRenderer;
        this.summaryIndent = summaryIndent;
        this.startAt1 = startAt1;
    }

    @Override
    public int getPageOffset()
    {
        return startAt1 ? 1 : 0;
    }

    public static @NonNull DefaultHelpCommandRenderer getDefault()
    {
        return DefaultHelpCommandRenderer.builder().build();
    }

    protected final int getCommandCount(final @NonNull Command command)
    {
        // TODO: Store this in the Command itself. Just do it on creation, then ensure that
        //       a) They cannot be modified after construction, or
        //       b) They recalculate it when they are modified.
        int count = command.isHidden() ? 0 : 1;
        for (final Command subCommand : command.getSubCommands())
            count += getCommandCount(subCommand);
        return count;
    }

    protected int getPageCount(final @NonNull Command command)
    {
        final int commandCount = getCommandCount(command);

        // Get the number of pages that can be filled using the provided number of commands
        // and the provided page size, excluding the number of commands put on the first page.
        return (int) Math.ceil((commandCount - firstPageSize) / (float) pageSize);
    }

    protected void renderPageCountHeader(final @NonNull Text text,
                                         final int page, final int pageCount)
    {
        text.add(String.format("------- Page (%2d / %2d) -------\n",
                               page + getPageOffset(), pageCount + getPageOffset()));
    }

    @Override
    public @NonNull Text render(final @NonNull ColorScheme colorScheme, final @NonNull Command command, final int page)
        throws IllegalValueException
    {
        final int pageCount = getPageCount(command);
        if (page > pageCount || page < 0)
            throw new IllegalValueException(command, Integer.toString(page), command.getCommandManager().isDebug());

        Text text = new Text(colorScheme);
        renderPageCountHeader(text, page, pageCount);
        if (page == 0)
            return renderFirstPage(colorScheme, text, command);

        final int skip = firstPageSize + (page - 1) * pageSize;
        renderCommands(colorScheme, text, getBaseSuperCommand(colorScheme, command), command, this.pageSize, skip);

        return text;
    }

    @Override
    public @NonNull Text render(final @NonNull ColorScheme colorScheme, final @NonNull Command command,
                                final @Nullable String val)
        throws IllegalValueException, CommandNotFoundException
    {
        if (val == null)
            return render(colorScheme, command, 0);

        final OptionalInt pageOpt = Util.parseInt(val);
        if (pageOpt.isPresent())
        // Subtract 1 from the desired page if counting starts at 1, because if the user specified page '1',
        // They should receive page '0' instead.
        {
            System.out.println("Selected page: " + pageOpt.getAsInt() + ", startAt1: " + startAt1 + ", final page: " +
                                   (pageOpt.getAsInt() - getPageOffset()));
            return render(colorScheme, command, pageOpt.getAsInt() - getPageOffset());
        }

        final @NonNull Optional<Command> subCommand = command.getCommandManager().getCommand(val);
        if (!subCommand.isPresent())
            throw new CommandNotFoundException(val, command.getCommandManager().isDebug());

        return renderLongCommand(colorScheme, subCommand.get());
    }

    @Override
    public @NonNull Text renderLongCommand(final @NonNull ColorScheme colorScheme, final @NonNull Command command)
    {
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
    public @NonNull Text renderFirstPage(final @NonNull ColorScheme colorScheme, final @NonNull Command command)
    {
        return renderFirstPage(colorScheme, new Text(colorScheme), command);
    }

    protected @NonNull Text renderFirstPage(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                            final @NonNull Command command)
    {
        if (displayHeader && !command.getHeader(colorScheme).equals(""))
            text.add(command.getHeader(colorScheme), TextType.HEADER).add("\n");

        renderCommands(colorScheme, text, getBaseSuperCommand(colorScheme, command), command, this.firstPageSize, 0);

        return text;
    }

    /**
     * Recursively renders the given command as well as all its subcommands.
     *
     * @param text          The {@link Text} to append the help to.
     * @param superCommands A {@link Text} with all the appended super commands of the current command. This will be
     *                      prepended to the command.
     * @param command       The {@link Command} and {@link Command#getSubCommands()} to render (recursively).
     * @param count         The number of {@link Command}s to render.
     * @return The number of commands that were added to the {@link Text}.
     */
    protected @NonNull Pair<Integer, Integer> renderCommands(final @NonNull ColorScheme colorScheme,
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
        if (!command.isHidden())
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
                renderCommands(colorScheme, text, newSuperCommands, subCommand, count - added, skip - skipped);

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
     * @param text          The {@link Text} to append the rendered command to.
     * @param command       The {@link Command} to render. Note that this method does not care about {@link
     *                      Command#isHidden()}.
     * @param superCommands A {@link Text} with all the appended super commands of the current command. This will be
     *                      prepended to the command.
     */
    protected void renderCommand(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                 final @NonNull Command command, final @NonNull Text superCommands)
    {
        text.add(superCommands).add(command.getName(), TextType.COMMAND);
        renderArgumentsShort(colorScheme, text, command);

        if (!command.getSummary(colorScheme).equals(""))
            text.add("\n").add(summaryIndent).add(command.getSummary(colorScheme), TextType.SUMMARY);
    }

    protected void renderArgumentsShort(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                        final @NonNull Command command)
    {
        for (final Argument<?> argument : command.getArgumentManager().getArguments())
            text.add(" ").add(argumentRenderer.render(colorScheme, argument));
    }

    protected void renderArgumentsLong(final @NonNull ColorScheme colorScheme, final @NonNull Text text,
                                       final @NonNull Command command)
    {
        for (final Argument<?> argument : command.getArgumentManager().getArguments())
            text.add("\n").add(argumentRenderer.renderLong(colorScheme, argument, summaryIndent));
    }
}
