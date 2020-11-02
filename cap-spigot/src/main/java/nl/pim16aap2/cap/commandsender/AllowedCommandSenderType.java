package nl.pim16aap2.cap.commandsender;

import org.bukkit.entity.Player;

/**
 * Represents a set of options for allowing commands for certain types of command senders (server and/or player).
 *
 * @author Pim
 */
public enum AllowedCommandSenderType
{
    /**
     * Only {@link Player}s are given access to a command.
     */
    PLAYER_ONLY,

    /**
     * Only non-{@link Player}s are given access to a command.
     */
    SERVER_ONLY,

    /**
     * Both {@link Player}s and non-{@link Player}s are given access to a command.
     */
    BOTH
}
