package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;

public class NonExistingArgumentException extends Exception
{
    @Getter
    private final String nonExistingArgument;

    public NonExistingArgumentException(final @NonNull String nonExistingArgument)
    {
        super();
        this.nonExistingArgument = nonExistingArgument;
    }
}

