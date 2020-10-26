package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.parser.PlayerParser;
import org.bukkit.entity.Player;

/**
 * Represents an argument that is parsed into a {@link Player} object.
 *
 * @author Pim
 */
public class PlayerArgument extends SpecializedArgument<Player>
{
    private static final @NonNull PlayerParser playerParser = PlayerParser.create();

    public PlayerArgument()
    {
        super(playerParser);
    }
}
