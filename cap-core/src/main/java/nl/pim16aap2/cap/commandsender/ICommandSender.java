package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;

/**
 * Represents an object that sent a command and can be interacted with.
 *
 * @author Pim
 */
public interface ICommandSender
{
    /**
     * Sends a message to this {@link ICommandSender}.
     *
     * @param message The message to send.
     */
    void sendMessage(final @NonNull Text message);

    /**
     * Gets the {@link ColorScheme} to use for all messages send to this {@link ICommandSender}.
     *
     * @return The {@link ColorScheme} to use for all messages send to this {@link ICommandSender}.
     */
    @NonNull ColorScheme getColorScheme();
}
