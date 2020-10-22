package nl.pim16aap2.commandparser.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;
import nl.pim16aap2.commandparser.exception.CommandParserException;
import nl.pim16aap2.commandparser.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.commandparser.renderer.IHelpCommandRenderer;
import nl.pim16aap2.commandparser.text.ColorScheme;

import java.util.Map;
import java.util.Optional;

import static nl.pim16aap2.commandparser.command.Command.DEFAULT_HELP_ARGUMENT;

@Getter
public class CommandResult
{
    private final @NonNull Command command;
    private final @NonNull Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments;
    private final HelpType helpType;
    private final @NonNull ICommandSender commandSender;

    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                         final @NonNull Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments)
    {
        this.commandSender = commandSender;
        this.command = command;
        this.parsedArguments = parsedArguments;

        // TODO: Clean this up.
        if (command instanceof DefaultHelpCommand)
            helpType = HelpType.NONE;
        else if (helpRequested(parsedArguments))
            helpType = HelpType.LONG_HELP;
        else
            helpType = HelpType.NONE;
    }

    private boolean helpRequested(final @NonNull Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments)
    {
        Argument.ParsedArgument<?> help = parsedArguments.get(DEFAULT_HELP_ARGUMENT.getName());
        if (help != null && help.getValue() != null)
            return true;

        Argument.ParsedArgument<?> longHelp = parsedArguments.get(DEFAULT_HELP_ARGUMENT.getLongName());
        return longHelp != null && longHelp.getValue() != null;
    }

    public @NonNull Optional<Argument.ParsedArgument<?>> getParsedArgumentOpt(final @NonNull String name)
    {
        return Optional.ofNullable(parsedArguments.get(name));
    }

    @SuppressWarnings("unchecked")
    public <T> T getParsedArgument(final @NonNull String name)
    {
        final Argument.ParsedArgument<T> result = (Argument.ParsedArgument<T>) parsedArguments.get(name);
        return result == null ? null : result.getValue();
    }

    public void run()
        throws CommandParserException
    {
        if (helpType == HelpType.NONE)
        {
            command.getCommandExecutor().accept(this);
            return;
        }

        final IHelpCommandRenderer helpCommand = DefaultHelpCommandRenderer.builder().build();

        if (helpType == HelpType.LONG_HELP)
        {
            commandSender.sendMessage(helpCommand.renderLongCommand(commandSender.getColorScheme(), command));
            return;
        }
    }

    public enum HelpType
    {
        /**
         * No help is requested at all.
         */
        NONE,

        /**
         * Help is requested for either a single or all sub{@link Command}s for the command.
         * <p>
         * If the value is empty or an integer, all sub{@link Command}s will be listed.
         * <p>
         * If the value is both not empty and not an integer, it will work like {@link #LONG_HELP}.
         * <p>
         * See {@link IHelpCommandRenderer#render(ColorScheme, Command, String)}.
         */
        SUBCOMMANDS,

        /**
         * Help is requested for this specific {@link Command}.
         */
        LONG_HELP
    }
}
