package nl.pim16aap2.cap;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

@AllArgsConstructor
class CommandListener implements Listener
{
    private final @NonNull SpigotCAP cap;

    private static final @NonNull Pattern SPACE_PATTERN = Pattern.compile(" ");

    /**
     * Checks if the first argument of a command is a super{@link Command} registered with out {@link CAP}.
     *
     * @param message The message to check.
     * @return True if the first argument is a registered super{@link Command}.
     */
    private boolean startsWithSuperCommand(final @Nullable Locale locale, final @NonNull String message)
    {
        try
        {
            final String superCommand = SPACE_PATTERN.split(message, 2)[0];
            return cap.getTopLevelCommand(superCommand, locale).isPresent();
        }
        catch (Throwable t)
        {
            // ignore
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(final @NonNull PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
            return;

        final String message = event.getMessage().substring(1);
        if (!startsWithSuperCommand(getLocale(event.getPlayer()), message))
            return;

        event.setCancelled(true);
        cap.parseInput(event.getPlayer(), message).ifPresent(CommandResult::run);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onServerCommand(final @NonNull ServerCommandEvent event)
    {
        if (!startsWithSuperCommand(getLocale(event.getSender()), event.getCommand()))
            return;

        event.setCancelled(true);
        cap.parseInput(event.getSender(), event.getCommand()).ifPresent(CommandResult::run);
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerCommandSendEvent(final @NonNull PlayerCommandSendEvent event)
    {
        // Disabled, because Bukkit is dumb af here and adding stuff is 'undefined' (it really isn't,
        // it just does nothing other than waste RAM and CPU cycles).
//        cap.getTopLevelCommandMap(cap.getCommandSenderFactory().getLocale(event.getPlayer()))
//           .keySet().forEach(cmdName -> event.getCommands().add("/" + cmdName));
    }

    @EventHandler(ignoreCancelled = true)
    void onTabCompletion(final @NonNull TabCompleteEvent event)
    {
        if (event.getBuffer().isEmpty() || !event.getBuffer().startsWith("/"))
            return;

        final @NonNull String command = event.getBuffer().substring(1); // Strip leading '/'.
        if (!startsWithSuperCommand(getLocale(event.getSender()), event.getBuffer().substring(1)))
            return;

        event.setCompletions(cap.getTabCompleteOptions(cap.getCommandSenderFactory()
                                                          .wrapCommandSender(event.getSender(), cap.getColorScheme()),
                                                       command));
    }

    private @Nullable Locale getLocale(final @NonNull CommandSender commandSender)
    {
        return cap.getCommandSenderFactory().getLocale(commandSender);
    }
}
