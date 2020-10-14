package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;

public class MissingArgumentException extends Exception
{
    @Getter
    private final String missingArgument;

    public MissingArgumentException(final @NonNull String missingArgument)
    {
        super();
        this.missingArgument = missingArgument;
    }
}

