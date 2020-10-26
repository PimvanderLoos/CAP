package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.parser.OfflinePlayerParser;
import org.bukkit.OfflinePlayer;

/**
 * Represents an argument that is parsed into an {@link OfflinePlayer} object.
 *
 * @author Pim
 */
public class OfflinePlayerArgument extends SpecializedArgument<OfflinePlayer>
{
    private static final @NonNull OfflinePlayerParser offlinePlayerParser = OfflinePlayerParser.create();

    public OfflinePlayerArgument()
    {
        super(offlinePlayerParser);
    }
}
