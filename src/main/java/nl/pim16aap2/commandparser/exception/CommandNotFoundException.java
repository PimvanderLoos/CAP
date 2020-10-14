package nl.pim16aap2.commandparser.exception;

import lombok.Getter;
import lombok.NonNull;

public class CommandNotFoundException extends Exception
{
    @Getter
    private final String missingCommand;

    public CommandNotFoundException(final @NonNull String commandName)
    {
        super();
        this.missingCommand = commandName;
    }
}
