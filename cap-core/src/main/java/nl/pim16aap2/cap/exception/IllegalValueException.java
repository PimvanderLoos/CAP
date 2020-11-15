package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;

@Getter
public class IllegalValueException extends CAPException
{
    private final String illegalValue;
    private final @NonNull Argument<?> argument;

    public IllegalValueException(final @NonNull Argument<?> argument, final @NonNull String illegalValue,
                                 final @NonNull String localizedMessage, final boolean stacktrace)
    {
        super(localizedMessage,
              "Received illegal value \"" + illegalValue + "\" for argument: " + argument.getIdentifier(), stacktrace);
        this.illegalValue = illegalValue;
        this.argument = argument;
    }
}

