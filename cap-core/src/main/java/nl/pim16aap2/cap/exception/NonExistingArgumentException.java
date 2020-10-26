package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;

public class NonExistingArgumentException extends CommandParserException
{
    @Getter
    private final @NonNull String nonExistingArgument;

    @Getter
    private final @NonNull Command command;

    public NonExistingArgumentException(final @NonNull Command command, final @NonNull String nonExistingArgument,
                                        final boolean stacktrace)
    {
        super("Argument \"" + nonExistingArgument + "\" does not exist for command: " + command.getName(), stacktrace);
        this.nonExistingArgument = nonExistingArgument;
        this.command = command;
    }
}
