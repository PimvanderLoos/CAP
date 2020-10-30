package nl.pim16aap2.cap.commandsender;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;

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
    public void sendMessage(final @NonNull Text message)
    {
        System.out.println(message);
    }

    @Override
    public boolean hasPermission(final @NonNull Command command)
    {
        return true;
    }
}
