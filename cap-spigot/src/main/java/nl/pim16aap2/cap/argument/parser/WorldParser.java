package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

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
    public @NonNull World parseArgument(final @NonNull String value)
    {
        @Nullable World world = null;
        try
        {
            world = Bukkit.getWorld(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            world = Bukkit.getWorld(value);
        }

        if (world == null)
            throw new IllegalArgumentException("World \"" + value + "\" could not be found!!");

        return world;
    }

    public static WorldParser create()
    {
        return new WorldParser();
    }
}
