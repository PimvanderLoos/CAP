package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.UUID;

/**
 * Represents an argument parser for {@link World} values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WorldParser extends ArgumentParser<World>
{
    @Override
    public @NonNull World parseArgument(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                                        final @NonNull Argument<?> argument, final @NonNull String value)
        throws IllegalValueException
    {
        @Nullable World world;
        try
        {
            world = Bukkit.getWorld(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            world = Bukkit.getWorld(value);
        }

        if (world == null)
        {
            // TODO: Spigot-specific error messages.
            final @NonNull String localizedMessage =
                MessageFormat.format(cap.getMessage("error.valueParser.integer", commandSender), value);
            throw new IllegalValueException(argument, value, localizedMessage, cap.isDebug());
        }

        return world;
    }

    public static WorldParser create()
    {
        return new WorldParser();
    }
}
