package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class CommandNotFoundException extends CommandParserException
{
    @Getter
    private final @NonNull String missingCommand;

    public CommandNotFoundException(final @Nullable String commandName, final boolean stacktrace)
    {
        super("Could not find command: \"" + getCommandName(commandName) + "\"", stacktrace);
        missingCommand = getCommandName(commandName);
    }

    public CommandNotFoundException(final @Nullable String commandName, final @NonNull String message,
                                    final boolean stacktrace)
    {
        super(message, stacktrace);
        missingCommand = getCommandName(commandName);
    }

    private static @NonNull String getCommandName(final @Nullable String commandName)
    {
        return commandName == null ? "NULL" : commandName;
    }

}
