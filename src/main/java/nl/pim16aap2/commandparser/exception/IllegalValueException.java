package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;

public class IllegalValueException extends CommandParserException
{
    @Getter
    private final String illegalValue;

    @Getter
    private final @NonNull Command command;

    public IllegalValueException(final @NonNull Command command, final @NonNull String illegalValue,
                                 final boolean stacktrace)
    {
        super("Received illegal value \"" + illegalValue + "\" for command: " + command.getName(), stacktrace);
        this.illegalValue = illegalValue;
        this.command = command;
    }

    public IllegalValueException(final @NonNull Command command, final @NonNull String illegalValue,
                                 final @NonNull Throwable cause, final boolean stacktrace)
    {
        super("Received illegal value \"" + illegalValue + "\" for command: " + command.getName(), cause, stacktrace);
        this.illegalValue = illegalValue;
        this.command = command;
    }
}

