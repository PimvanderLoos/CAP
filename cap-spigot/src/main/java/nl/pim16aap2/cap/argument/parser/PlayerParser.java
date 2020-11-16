package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * Represents an argument parser for online {@link Player} values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerParser extends ArgumentParser<Player>
{
    @Override
    public @NonNull Player parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                         final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException
    {
        @Nullable Player player;
        try
        {
            player = Bukkit.getPlayer(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            player = Bukkit.getPlayer(value);
        }

        if (player == null)
        {
            // TODO: Spigot-specific error messages.
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getLocalizer().getMessage("error.valueParser.integer", commandSender), value);
            throw new IllegalValueException(argument, value, localizedMessage, cap.isDebug());
        }

        return player;
    }

    public static PlayerParser create()
    {
        return new PlayerParser();
    }
}
