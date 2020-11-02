package nl.pim16aap2.cap;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.SpigotCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.TabCompleteEvent;

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
    private boolean startsWithSuperCommand(final @NonNull String message)
    {
        try
        {
            final String superCommand = SPACE_PATTERN.split(message, 2)[0];
            return cap.getSuperCommand(superCommand).isPresent();
        }
        catch (Throwable t)
        {
            // ignore
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(final @NonNull PlayerCommandPreprocessEvent event)
    {
        if (event.isCancelled())
            return;

        final String message = event.getMessage().substring(1);
        if (!startsWithSuperCommand(message))
            return;

        event.setCancelled(true);
        cap.parseInput(event.getPlayer(), message).ifPresent(CommandResult::run);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(final @NonNull ServerCommandEvent event)
    {
        if (!startsWithSuperCommand(event.getCommand()))
            return;

        event.setCancelled(true);
        cap.parseInput(event.getSender(), event.getCommand()).ifPresent(CommandResult::run);
    }

    @EventHandler
    void onTabCompletion(final @NonNull TabCompleteEvent event)
    {
        if (event.getBuffer().isEmpty() || !event.getBuffer().startsWith("/"))
            return;

        final @NonNull String command = event.getBuffer().substring(1); // Strip leading '/'.
        if (!startsWithSuperCommand(event.getBuffer().substring(1)))
            return;

        event.setCompletions(cap.getTabCompleteOptions(SpigotCommandSender.wrapCommandSender(event.getSender(),
                                                                                             cap.getColorScheme()),
                                                       command));
    }
}