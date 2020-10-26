package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SpigotCommandSender
{
    private static final @NonNull ColorScheme EMPTY_COLOR_SCHEME = ColorScheme.builder().build();

    /**
     * Wraps a {@link CommandSender} with an {@link ICommandSender} as used by CAP.
     * <p>
     * This method uses an empty {@link ColorScheme}.
     *
     * @param commandSender The {@link CommandSender} to wrap.
     * @return A {@link ICommandSender} that can be used by CAP.
     */
    public static @NonNull ICommandSender wrapCommandSender(final @NonNull CommandSender commandSender)
    {
        return wrapCommandSender(commandSender, null);
    }

    /**
     * Wraps a {@link CommandSender} with an {@link ICommandSender} as used by CAP.
     *
     * @param commandSender The {@link CommandSender} to wrap.
     * @param colorScheme   The {@link ColorScheme} to use for generating messages.
     *                      <p>
     *                      Note that this only applies to players!
     * @return A {@link ICommandSender} that can be used by CAP.
     */
    public static @NonNull ICommandSender wrapCommandSender(final @NonNull CommandSender commandSender,
                                                            @Nullable ColorScheme colorScheme)
    {
        colorScheme = colorScheme == null ? EMPTY_COLOR_SCHEME : colorScheme;

        if (commandSender instanceof Player)
            return new SpigotPlayerCommandSender((Player) commandSender, colorScheme);
        return new SpigotServerCommandSender();
    }
}
