package nl.pim16aap2.commandparser.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;

import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
@Getter
public class CommandResult
{
    private final @NonNull Command command;
    private final @NonNull Map<@NonNull String, Argument.ParsedArgument<?>> parsedArguments;

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
    {
        command.getCommandExecutor().accept(this);
    }
}
