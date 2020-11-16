package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * Represents an argument parser for {@link OfflinePlayer} values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OfflinePlayerParser extends ArgumentParser<OfflinePlayer>
{
    @Override
    public @NonNull OfflinePlayer parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                                final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException
    {
        try
        {
            return Bukkit.getOfflinePlayer(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            // TODO: Spigot-specific error messages.
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getLocalizer().getMessage("error.valueParser.integer", commandSender), value);
            throw new IllegalValueException(argument, value, localizedMessage, cap.isDebug());
        }
    }

    public static OfflinePlayerParser create()
    {
        return new OfflinePlayerParser();
    }
}
