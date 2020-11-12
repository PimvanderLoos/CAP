package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;

@Getter
public class ValidationFailureException extends CAPException
{
    private final @NonNull Argument<?> argument;
    private final @NonNull String value;
    private final @NonNull String localizedMessage;

    public ValidationFailureException(final @NonNull Argument<?> argument, final @NonNull String value,
                                      final @NonNull String localizedMessage, final boolean stacktraceEnabled)
    {
        super(stacktraceEnabled);
        this.argument = argument;
        this.value = value;
        this.localizedMessage = localizedMessage;
    }
}
