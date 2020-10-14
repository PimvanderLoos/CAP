package nl.pim16aap2.commandparser.commandline.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.commandline.argument.Argument;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public Argument.ParsedArgument<?> getParsedArgument(final @NonNull String name)
    {
        return parsedArguments.get(name);
    }

    public void run()
    {
        command.getCommandExecutor().accept(this);
    }
}
