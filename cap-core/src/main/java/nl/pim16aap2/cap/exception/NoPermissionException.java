package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;

@Getter
public class NoPermissionException extends CAPException
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
