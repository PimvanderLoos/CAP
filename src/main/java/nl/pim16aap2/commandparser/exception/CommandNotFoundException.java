package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public class CommandNotFoundException extends CommandParserException
{
    @Getter
    private final @NonNull String missingCommand;

    public CommandNotFoundException(final @Nullable String commandName)
    {
        super();
        this.missingCommand = commandName == null ? "NULL" : commandName;
    }
}
