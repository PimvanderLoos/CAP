package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;

public class MissingArgumentException extends CommandParserException
{
    @Getter
    private final String missingArgument;

    @Getter
    private final @NonNull Command command;

    public MissingArgumentException(final @NonNull Command command, final @NonNull String missingArgument,
                                    final boolean stacktrace)
    {
        super("No value found for argument \"" + missingArgument + "\" of command: " + command.getName(), stacktrace);
        this.missingArgument = missingArgument;
        this.command = command;
        System.out.println("Created new MissingArgumentException! Stacktrace? " + stacktrace);
    }
}

