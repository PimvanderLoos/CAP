package nl.pim16aap2.commandparser.exception;

import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.commandsender.ICommandSender;

public class NoPermissionException extends CommandParserException
{
    private final @NonNull ICommandSender commandSender;
    private final @NonNull Command command;

    public NoPermissionException(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                                 final boolean stacktraceEnabled)
    {
        super(stacktraceEnabled);
        this.commandSender = commandSender;
        this.command = command;
    }
}
