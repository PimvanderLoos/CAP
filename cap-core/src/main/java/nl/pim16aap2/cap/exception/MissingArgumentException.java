package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;

@Getter
public class MissingArgumentException extends CAPException
{
    private final Argument<?> missingArgument;
    private final @NonNull Command command;

    public MissingArgumentException(final @NonNull Command command, final @NonNull Argument<?> missingArgument,
                                    final @NonNull String localizedMessage, final boolean stacktrace)
    {
        super(localizedMessage, "No value found for argument \"" + missingArgument.getIdentifier() +
            "\" of command: " + command.getNameKey(), stacktrace);
        this.missingArgument = missingArgument;
        this.command = command;
    }
}

