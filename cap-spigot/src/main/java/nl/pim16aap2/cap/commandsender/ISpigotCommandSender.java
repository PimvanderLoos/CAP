package nl.pim16aap2.cap.commandsender;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an {@link ICommandSender} for the Spigot platform.
 *
 * @author Pim
 */
public interface ISpigotCommandSender extends ICommandSender
{
    /**
     * Gets the Spigot {@link CommandSender} represented by this {@link ICommandSender}.
     * <p>
     * If this is the server, the returned value will be null.
     *
     * @return The {@link CommandSender} represented by this object or null if it is the server.
     */
    @Nullable CommandSender getCommandSender();
}
