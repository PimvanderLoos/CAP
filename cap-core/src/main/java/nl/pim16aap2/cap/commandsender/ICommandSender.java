package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents an object that sent a command and can be interacted with.
 *
 * @author Pim
 */
public interface ICommandSender
{
    /**
     * Gets the {@link Locale} for this {@link ICommandSender}.
     *
     * @return The selected {@link Locale}. (Default: null).
     */
    default @Nullable Locale getLocale()
    {
        return null;
    }

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
