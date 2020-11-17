package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import org.bukkit.command.CommandSender;

/**
 * Represents an {@link ICommandSender} for the Spigot platform.
 *
 * @author Pim
 */
public interface ISpigotCommandSender extends ICommandSender
{
    /**
     * Gets the Spigot {@link CommandSender} represented by this {@link ICommandSender}.
     *
     * @return The {@link CommandSender} represented by this object.
     */
    @NonNull CommandSender getCommandSender();
}
