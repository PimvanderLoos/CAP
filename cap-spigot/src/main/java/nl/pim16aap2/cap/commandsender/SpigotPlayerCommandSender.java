package nl.pim16aap2.cap.commandsender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.SpigotTextUtility;
import nl.pim16aap2.cap.text.Text;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class SpigotPlayerCommandSender implements ICommandSender
{
    @Getter
    protected final @NonNull Player player;

    @Getter(onMethod = @__({@Override}))
    protected final @NonNull ColorScheme colorScheme;

    @Override
    public void sendMessage(final @NonNull Text message)
    {
        player.spigot().sendMessage(SpigotTextUtility.toBaseComponents(message));
    }
}
