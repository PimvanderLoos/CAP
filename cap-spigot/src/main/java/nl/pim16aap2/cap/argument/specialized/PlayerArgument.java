package nl.pim16aap2.cap.argument.specialized;

import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.RepeatableArgument;
import nl.pim16aap2.cap.argument.parser.PlayerParser;
import nl.pim16aap2.cap.util.SpigotUtil;
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

    @Override
    public Argument.OptionalBuilder<Player> getOptional()
    {
        return super.getOptional().tabcompleteFunction(SpigotUtil.onlinePlayersTabcompletion());
    }

    @Override
    public Argument.@NonNull RequiredBuilder<Player> getRequired()
    {
        return super.getRequired().tabcompleteFunction(SpigotUtil.onlinePlayersTabcompletion());
    }

    @Override
    public Argument.@NonNull OptionalPositionalBuilder<Player> getOptionalPositional()
    {
        return super.getOptionalPositional().tabcompleteFunction(SpigotUtil.onlinePlayersTabcompletion());
    }

    @Override
    public RepeatableArgument.@NonNull RepeatableArgumentBuilder<Player> getRepeatable()
    {
        return super.getRepeatable().tabcompleteFunction(SpigotUtil.onlinePlayersTabcompletion());
    }
}
