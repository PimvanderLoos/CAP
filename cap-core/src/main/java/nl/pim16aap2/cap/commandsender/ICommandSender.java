package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
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
    default void sendMessage(final @NonNull Text message)
    {
        sendMessage(message.toString());
    }

    /**
     * Sends a message to this {@link ICommandSender}.
     *
     * @param message The message to send.
     */
    void sendMessage(final @NonNull String message);

    /**
     * Checks if this {@link ICommandSender} has permission to access the given {@link Command}.
     *
     * @param command The {@link Command} to check.
     * @return True if this {@link ICommandSender} has access to the given {@link Command}.
     */
    boolean hasPermission(final @NonNull Command command);

    /**
     * Gets the {@link ColorScheme} to use for all messages send to this {@link ICommandSender}.
     *
     * @return The {@link ColorScheme} to use for all messages send to this {@link ICommandSender}.
     */
    @NonNull ColorScheme getColorScheme();
}
