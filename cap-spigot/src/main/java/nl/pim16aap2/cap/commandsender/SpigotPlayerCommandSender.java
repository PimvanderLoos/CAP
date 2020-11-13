package nl.pim16aap2.cap.commandsender;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.SpigotTextUtility;
import nl.pim16aap2.cap.text.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@AllArgsConstructor
public class SpigotPlayerCommandSender implements ISpigotCommandSender
{
    @Getter
    protected final @NonNull Player player;

    @Getter(onMethod = @__({@Override}))
    protected final @NonNull ColorScheme colorScheme;

    protected @Nullable Locale locale;

    @Override
    public void sendMessage(final @NonNull Text message)
    {
        player.spigot().sendMessage(SpigotTextUtility.toBaseComponents(message));
    }

    @Override
    public @Nullable Locale getLocale()
    {
        return locale;
    }

    @Override
    public int hashCode()
    {
        return player.hashCode();
    }

    @Override
    public String toString()
    {
        return player.getName() + " (" + player.getUniqueId().toString() + ")";
    }

    @Override
    public @Nullable CommandSender getCommandSender()
    {
        return player;
    }
}
