package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

@Getter
public class CommandNotFoundException extends CAPException
{
    private final @NonNull String missingCommand;
    private final @NonNull String localizedMessage;

    public CommandNotFoundException(final @Nullable String commandName, final @NonNull String localizedMessage,
                                    final boolean stacktrace)
    {
        super("Could not find command: \"" + getCommandName(commandName) + "\"", stacktrace);
        missingCommand = getCommandName(commandName);
        this.localizedMessage = localizedMessage;
    }

    private static @NonNull String getCommandName(final @Nullable String commandName)
    {
        return commandName == null ? "NULL" : commandName;
    }

}
