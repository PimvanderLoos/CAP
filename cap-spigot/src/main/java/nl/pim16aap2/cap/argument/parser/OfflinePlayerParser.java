package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
    public @NonNull OfflinePlayer parseArgument(final @NonNull String value)
    {
        try
        {
            return Bukkit.getOfflinePlayer(UUID.fromString(value));
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("OfflinePlayer \"" + value + "\" Could not be found!");
        }
    }

    public static OfflinePlayerParser create()
    {
        return new OfflinePlayerParser();
    }
}
