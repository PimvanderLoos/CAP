package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;

@Getter
public class IllegalValueException extends CAPException
{
    private final String illegalValue;
    private final @NonNull Command command;

    public IllegalValueException(final @NonNull Command command, final @NonNull String illegalValue,
                                 final @NonNull String localizedMessage, final boolean stacktrace)
    {
        super(localizedMessage,
              "Received illegal value \"" + illegalValue + "\" for command: " + command.getNameKey(), stacktrace);
        this.illegalValue = illegalValue;
        this.command = command;
    }

    public IllegalValueException(final @NonNull Command command, final @NonNull String illegalValue,
                                 final @NonNull Throwable cause, final @NonNull String localizedMessage,
                                 final boolean stacktrace)
    {
        super(localizedMessage,
              "Received illegal value \"" + illegalValue + "\" for command: " + command.getNameKey(), cause,
              stacktrace);
        this.illegalValue = illegalValue;
        this.command = command;
    }
}

