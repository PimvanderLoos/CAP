package nl.pim16aap2.commandparser.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.IllegalValueException;
import nl.pim16aap2.commandparser.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.commandparser.renderer.IHelpCommandRenderer;

import java.util.Map;
import java.util.Optional;

import static nl.pim16aap2.commandparser.command.Command.DEFAULT_HELP_ARGUMENT;

@Getter
public class CommandResult
{
    private final @NonNull Command command;
    private final @NonNull Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments;
    private HelpType helpType;

    public CommandResult(final @NonNull Command command,
                         final @NonNull Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments)
    {
        this.command = command;
        this.parsedArguments = parsedArguments;
        if (parsedArguments.get(DEFAULT_HELP_ARGUMENT.getName()) != null ||
            parsedArguments.get(DEFAULT_HELP_ARGUMENT.getLongName()) != null)
            helpType = HelpType.LONG_HELP;
        else
            helpType = HelpType.NONE;
        // TODO: The HelpCommand should be an actual command and its executor should take care of the third option.
        //       This won't require any updates to the parser.
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
        throws IllegalValueException, CommandNotFoundException
    {
        if (helpType == HelpType.NONE)
        {
            command.getCommandExecutor().accept(this);
            return;
        }

        // TODO: Request this from the command.
        IHelpCommandRenderer helpCommand = DefaultHelpCommandRenderer.builder().colorScheme(
            getCommand().getCommandManager().getColorScheme().get()).build();

        if (helpType == HelpType.LONG_HELP)
        {
            command.getCommandManager().getTextConsumer().accept(helpCommand.renderLongCommand(command));
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
         * See {@link IHelpCommandRenderer#render(Command, String)}.
         */
        SUBCOMMANDS,

        /**
         * Help is requested for this specific {@link Command}.
         */
        LONG_HELP
    }
}
