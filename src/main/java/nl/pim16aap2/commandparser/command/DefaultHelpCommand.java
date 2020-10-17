package nl.pim16aap2.commandparser.command;

//public TextComponent render(final @NonNull Command command, final int page, final int pageSize,
//final int firstPageSize)

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.renderer.ArgumentRenderer;
import nl.pim16aap2.commandparser.renderer.ColorScheme;
import nl.pim16aap2.commandparser.renderer.TextComponent;
import nl.pim16aap2.commandparser.renderer.TextType;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the default {@link IHelpCommand}.
 *
 * @author Pim
 */
@Builder
@Getter
public class DefaultHelpCommand implements IHelpCommand
{
    // TODO: Make this configurable
    private static final String COMMAND_PREFIX = "/";

    /**
     * The {@link ColorScheme} to use for the help text generated by this {@link IHelpCommand}
     */
    private final @NonNull ColorScheme colorScheme;

    /**
     * The number of subcommands to display per page. Default = 5.
     */
    @Builder.Default
    private int pageSize = 5;

    /**
     * The number of subcommands on the first page. Default = 1.
     */
    @Builder.Default
    private int firstPageSize = 1;

    /**
     * Whether or not to display the header of the command on the first page. Default = true.
     */
    @Builder.Default
    private boolean displayHeader = true;

    /**
     * The string to prepend before every summary. Default = "  ".
     */
    @Builder.Default
    private String summaryIndent = "  ";

    /**
     * Whether or not to display the arguments of each command on the list page.
     */
    @Builder.Default
    private boolean displayArgumentsForSimple = false;

    protected @NonNull ArgumentRenderer argumentRenderer;

    public DefaultHelpCommand(final @NonNull ColorScheme colorScheme, final int pageSize, final int firstPageSize,
                              final boolean displayHeader, final @NonNull String summaryIndent,
                              final boolean displayArgumentsForSimple,
                              final @Nullable ArgumentRenderer argumentRenderer)
    {
        this.colorScheme = colorScheme;
        this.pageSize = pageSize;
        this.firstPageSize = firstPageSize;
        this.displayHeader = displayHeader;
        this.displayArgumentsForSimple = displayArgumentsForSimple;
        this.argumentRenderer = argumentRenderer == null ? new ArgumentRenderer(colorScheme) : argumentRenderer;
        this.summaryIndent = summaryIndent;
    }

    @Override
    public @NonNull TextComponent render(final @NonNull Command command, final int page)
    {
        if (page == 0)
            return renderFirstPage(command);

        return new TextComponent(colorScheme);
    }

    @Override
    public @NonNull TextComponent render(@NonNull Command command)
    {
        // TODO: Why does this exist?
        return render(command, 0);
    }

    protected @NonNull TextComponent renderFirstPage(final @NonNull Command command)
    {
        final TextComponent textComponent = new TextComponent(colorScheme);
        if (displayHeader && !command.getHeader().equals(""))
            textComponent.add(command.getHeader(), TextType.HEADER).add("\n");

        final TextComponent superCommands = new TextComponent(colorScheme).add(COMMAND_PREFIX, TextType.COMMAND);

        renderCommands(textComponent, superCommands, command, this.firstPageSize);

        return textComponent;
    }

    /**
     * Recursively renders the given command as well as all its subcommands.
     *
     * @param textComponent The {@link TextComponent} to append the help to.
     * @param superCommands A {@link TextComponent} with all the appended super commands of the current command. This
     *                      will be prepended to the command.
     * @param command       The {@link Command} and {@link Command#getSubCommands()} to render (recursively).
     * @param count         The number of {@link Command}s to render.
     * @return The number of commands that were added to the {@link TextComponent}.
     */
    // TODO: Add a number of (sub)commands to skip
    protected int renderCommands(final @NonNull TextComponent textComponent, final @NonNull TextComponent superCommands,
                                 final @NonNull Command command, final int count)
    {
        int added = 0;
        if (count < 1)
            return added;

        if (!command.isHidden())
        {
            ++added;
            textComponent.add(superCommands).add(command.getName(), TextType.COMMAND);

            // TODO: This should not be hardcoded like this.
            for (final Argument<?> argument : command.getRequiredArguments().values())
                textComponent.add(" ").add(argumentRenderer.render(argument));

            for (final Argument<?> argument : command.getOptionalArguments().values())
                textComponent.add(" ").add(argumentRenderer.render(argument));

            for (final Argument<?> argument : command.getRepeatableArguments().values())
                textComponent.add(" ").add(argumentRenderer.render(argument));

            textComponent.add("\n" + summaryIndent).add(command.getSummary(), TextType.SUMMARY).add("\n");
        }

        if (added == count)
            return added;

        final TextComponent newSuperCommands =
            new TextComponent(superCommands).add(command.getName(), TextType.COMMAND).add(" ");

        for (final Command subCommand : command.getSubCommands().values())
        {
            added += renderCommands(textComponent, newSuperCommands, subCommand, count - added);
            if (added >= count)
                break;
        }

        return added;
    }


}
