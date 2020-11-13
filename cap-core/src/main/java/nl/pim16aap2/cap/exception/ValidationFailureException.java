package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import org.jetbrains.annotations.Nullable;

@Getter
public class ValidationFailureException extends CAPException
{
    private final @Nullable Argument<?> argument;
    private final @NonNull String value;

    public ValidationFailureException(final @Nullable Argument<?> argument, final @NonNull String value,
                                      final @NonNull String localizedMessage, final boolean stacktraceEnabled)
    {
        super(localizedMessage, stacktraceEnabled);
        this.argument = argument;
        this.value = value;
    }

    public ValidationFailureException(final @NonNull String value, final @NonNull String localizedMessage,
                                      final boolean stacktraceEnabled)
    {
        super(localizedMessage, stacktraceEnabled);
        argument = null;
        this.value = value;
    }
}
