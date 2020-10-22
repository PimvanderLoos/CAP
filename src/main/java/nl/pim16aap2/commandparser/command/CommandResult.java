package nl.pim16aap2.commandparser.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;
import nl.pim16aap2.commandparser.exception.CommandParserException;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

@Getter
public class CommandResult
{
    private final @NonNull Command command;
    private final @Nullable Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments;
    private final @NonNull ICommandSender commandSender;

    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                         final @Nullable Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments)
    {
        this.commandSender = commandSender;
        this.command = command;
        this.parsedArguments = parsedArguments;
    }

    public CommandResult(final @NonNull ICommandSender commandSender, final @NonNull Command command)
    {
        this(commandSender, command, null);
    }

    /**
     * Checks if help is required for the user. This is completely unrelated to the help command/argument, but rather to
     * incorrect usage of the current command (e.g. missing required argument(s)).
     *
     * @return True if the help message is to be sent to the user if {@link #run()} were to be called now.
     */
    public boolean helpRequired()
    {
        if (this.parsedArguments == null)
            return true;
        final @NonNull Optional<Argument.ParsedArgument<Boolean>> helpArg = getParsedArgumentOpt("h");
        return helpArg.isPresent() && helpArg.get().getValue() != null;
    }

    @SuppressWarnings("unchecked")
    public @NonNull <T> Optional<Argument.ParsedArgument<T>> getParsedArgumentOpt(final @NonNull String name)
    {
        if (parsedArguments == null)
            return Optional.empty();
        return Optional.ofNullable((Argument.ParsedArgument<T>) parsedArguments.get(name));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getParsedArgument(final @NonNull String name)
    {
        // TODO: Handle this a bit better, maybe? This method shouldn't
        //       really be called if parsedArguments is null anyway.
        if (parsedArguments == null)
            return null;
        final Argument.ParsedArgument<T> result = (Argument.ParsedArgument<T>) parsedArguments.get(name);
        return result == null ? null : result.getValue();
    }

    public void run()
        throws CommandParserException
    {
        if (helpRequired())
            command.sendHelp(commandSender);
        else
            command.getCommandExecutor().accept(this);
    }
}
