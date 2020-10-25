package nl.pim16aap2.commandparser.exception;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;

public class ValidationFailureException extends CommandParserException
{
    private final @NonNull Argument<?> argument;
    private final @NonNull String value;

    public ValidationFailureException(final @NonNull Argument<?> argument, final @NonNull String value,
                                      final boolean stacktraceEnabled)
    {
        super(stacktraceEnabled);
        this.argument = argument;
        this.value = value;
    }
}
