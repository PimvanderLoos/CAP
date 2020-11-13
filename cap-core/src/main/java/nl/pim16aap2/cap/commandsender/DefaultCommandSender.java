package nl.pim16aap2.cap.commandsender;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
    public int hashCode()
    {
        // A hashcode of 0 works fine, because this command sender interacts directly with sysout,
        // which is assumed to be just 1 user. This is only a default implementation anyway, so
        // it doesn't matter that this may not cover every use case.
        return 0;
    }

    @Override
    public String toString()
    {
        return "Default Command Sender";
    }
}
