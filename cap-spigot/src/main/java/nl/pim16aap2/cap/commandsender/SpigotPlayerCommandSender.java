package nl.pim16aap2.cap.commandsender;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.text.ColorScheme;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class SpigotPlayerCommandSender implements ICommandSender
{
    protected final @NonNull Player player;

    protected final @NonNull ColorScheme colorScheme;

    @Override
    public void sendMessage(final @NonNull String message)
    {
        player.sendMessage(message);
    }

    @Override
    public boolean hasPermission(final @NonNull Command command)
    {
        if (command.getPermission() == null)
            return true;
        return player.hasPermission(command.getPermission());
    }

    @Override
    public @NonNull ColorScheme getColorScheme()
    {
        return colorScheme;
    }
}