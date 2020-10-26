package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

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
    public @NonNull Player parseArgument(final @NonNull String value)
    {
        @Nullable Player player = null;
        try
        {
            player = Bukkit.getPlayer(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            player = Bukkit.getPlayer(value);
        }

        if (player == null)
            throw new IllegalArgumentException("Player \"" + value + "\" could not be found!!");

        return player;
    }

    public static PlayerParser create()
    {
        return new PlayerParser();
    }
}
