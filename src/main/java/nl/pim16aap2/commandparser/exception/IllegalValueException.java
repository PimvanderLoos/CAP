package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;

public class IllegalValueException extends Exception
{
    @Getter
    private final String illegalValue;

    @Getter
    private final @NonNull Command command;

    public IllegalValueException(final @NonNull Command command, final @NonNull String illegalValue)
    {
        super();
        this.illegalValue = illegalValue;
        this.command = command;
    }
}

