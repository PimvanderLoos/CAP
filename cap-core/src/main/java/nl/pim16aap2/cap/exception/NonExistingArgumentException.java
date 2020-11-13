package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;

@Getter
public class NonExistingArgumentException extends CAPException
{
    private final @NonNull String nonExistingArgument;
    private final @NonNull Command command;

    public NonExistingArgumentException(final @NonNull Command command, final @NonNull String nonExistingArgument,
                                        final @NonNull String localizedMessage, final boolean stacktrace)
    {
        super(localizedMessage,
              "Argument \"" + nonExistingArgument + "\" does not exist for command: " + command.getNameKey(),
              stacktrace);
        this.nonExistingArgument = nonExistingArgument;
        this.command = command;
    }
}

