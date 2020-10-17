package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;

public class MissingArgumentException extends Exception
{
    @Getter
    private final String missingArgument;

    @Getter
    private final @NonNull Command command;

    public MissingArgumentException(final @NonNull Command command, final @NonNull String missingArgument)
    {
        super();
        this.missingArgument = missingArgument;
        this.command = command;
    }
}

