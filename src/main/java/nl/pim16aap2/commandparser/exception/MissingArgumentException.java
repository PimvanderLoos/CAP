package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.command.Command;

public class MissingArgumentException extends CommandParserException
{
    @Getter
    private final Argument<?> missingArgument;

    @Getter
    private final @NonNull Command command;

    public MissingArgumentException(final @NonNull Command command, final @NonNull Argument<?> missingArgument,
                                    final boolean stacktrace)
    {
        super("No value found for argument \"" + missingArgument.getName() +
                  "\" of command: " + command.getName(), stacktrace);
        this.missingArgument = missingArgument;
        this.command = command;
    }
}

