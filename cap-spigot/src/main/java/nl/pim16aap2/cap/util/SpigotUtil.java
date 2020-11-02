package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.AllowedCommandSenderType;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.commandsender.SpigotPlayerCommandSender;
import nl.pim16aap2.cap.commandsender.SpigotServerCommandSender;

import java.util.function.BiFunction;

/**
 * Represents a set of utility methods used for the Spigot platform.
 *
 * @author Pim
 */
@UtilityClass
public class SpigotUtil
{
    /**
     * Gets the permission function for {@link AllowedCommandSenderType#SERVER_ONLY}.
     *
     * @return The permission function based on the provided specifications.
     */
    public BiFunction<ICommandSender, Command, Boolean> getPermissionFunction()
    {
        return (sender, cmd) -> sender instanceof SpigotServerCommandSender;
    }

    /**
     * Gets the permission function. Defaults to {@link AllowedCommandSenderType#BOTH}.
     *
     * @param node The permission node to check. E.g. "myplugin.teleport"
     * @return The permission function based on the provided specifications.
     */
    public BiFunction<ICommandSender, Command, Boolean> getPermissionFunction(final @NonNull String node)
    {
        return (sender, cmd) -> SpigotUtil.hasPermission(sender, cmd, node, AllowedCommandSenderType.BOTH);
    }

    /**
     * Gets the permission function.
     *
     * @param node The permission node to check. E.g. "myplugin.teleport"
     * @param type The type of access to check.
     *             <p>
     *             For example, when set to {@link AllowedCommandSenderType#SERVER_ONLY}, the command can only be
     *             executed by non-players. Any player who tries to execute the command will fail the permission check
     *             (including OPs).
     * @return The permission function based on the provided specifications.
     */
    public BiFunction<ICommandSender, Command, Boolean> getPermissionFunction(final @NonNull String node,
                                                                              final @NonNull AllowedCommandSenderType type)
    {
        return (sender, cmd) -> SpigotUtil.hasPermission(sender, cmd, node, type);
    }

    /**
     * Checks if an {@link ICommandSender} has access to a {@link Command}.
     *
     * @param commandSender The {@link ICommandSender} whose permission access to check.
     * @param command       The {@link Command} to check access for.
     * @param node          The permission node to check. E.g. "myplugin.teleport"
     * @param type          The type of access to check.
     * @return True if the {@link ICommandSender} has access to the {@link Command}.
     */
    public static boolean hasPermission(final @NonNull ICommandSender commandSender, final @NonNull Command command,
                                        final @NonNull String node, final @NonNull AllowedCommandSenderType type)
    {
        if (type != AllowedCommandSenderType.BOTH)
        {
            if (type == AllowedCommandSenderType.PLAYER_ONLY && commandSender instanceof SpigotServerCommandSender)
                return false;
            if (type == AllowedCommandSenderType.SERVER_ONLY && commandSender instanceof SpigotPlayerCommandSender)
                return false;
        }

        if (commandSender instanceof SpigotPlayerCommandSender)
            return ((SpigotPlayerCommandSender) commandSender).getPlayer().hasPermission(node);

        return true;
    }
}
