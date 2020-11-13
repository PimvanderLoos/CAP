package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class CommandNotFoundException extends CAPException
{
    private final @NonNull String missingCommand;
    private final @NonNull String localizedMessage;

    public CommandNotFoundException(final @NonNull String commandName, final @NonNull String localizedMessage,
                                    final boolean stacktrace)
    {
        super(localizedMessage, "Could not find command: \"" + commandName + "\"", stacktrace);
        missingCommand = commandName;
        this.localizedMessage = localizedMessage;
    }
}
