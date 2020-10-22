package nl.pim16aap2.commandparser.commandsender;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.text.ColorScheme;

/**
 * Represents a basic implementation of {@link ICommandSender}.
 * <p>
 * It has access to all commands and sends plain text messages to stdout.
 *
 * @author Pim
 */
public class DefaultCommandSender implements ICommandSender
{
    @Setter
    @Getter
    protected ColorScheme colorScheme = ColorScheme.builder().build();

    @Override
    public void sendMessage(final @NonNull String message)
    {
        System.out.println(message);
    }

    @Override
    public boolean hasPermission(final @NonNull Command command)
    {
        return true;
    }
}
