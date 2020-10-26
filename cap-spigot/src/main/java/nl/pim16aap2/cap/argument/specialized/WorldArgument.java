package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.parser.WorldParser;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Represents an argument that is parsed into a {@link Player} object.
 *
 * @author Pim
 */
public class WorldArgument extends SpecializedArgument<World>
{
    private static final @NonNull WorldParser worldParser = WorldParser.create();

    public WorldArgument()
    {
        super(worldParser);
    }
}
