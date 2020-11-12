package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;

@Getter
public class MissingValueException extends CAPException
{
    private final @NonNull Argument<?> argument;
    private final @NonNull Command command;

    public MissingValueException(final @NonNull Command command, final @NonNull Argument<?> argument,
                                 final boolean stacktrace)
    {
        super("Missing value for argument \"" + argument.getIdentifier() + "\" for command: " + command.getNameKey(),
              stacktrace);
        this.argument = argument;
        this.command = command;
    }

    public MissingValueException(final @NonNull Command command, final @NonNull Argument<?> argument,
                                 final @NonNull Throwable cause, final boolean stacktrace)
    {
        super("Missing value for argument \"" + argument.getIdentifier() + "\" for command: " + command.getNameKey(),
              cause, stacktrace);
        this.argument = argument;
        this.command = command;
    }
}

