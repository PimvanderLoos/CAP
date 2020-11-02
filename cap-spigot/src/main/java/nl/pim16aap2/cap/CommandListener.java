package nl.pim16aap2.cap;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.CommandParserException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.MissingArgumentException;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.exception.NonExistingArgumentException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.io.EOFException;
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

        // TODO: Use Exception handler.
        try
        {
            cap.parseInput(event.getPlayer(), message).run();
        }
        catch (CommandNotFoundException e)
        {
            System.out.println("Failed to find command: " + e.getMissingCommand());
            e.printStackTrace();
        }
        catch (NonExistingArgumentException e)
        {
            System.out.println("Failed to find argument: \"" + e.getNonExistingArgument() + "\" for command: \"" +
                                   e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (MissingArgumentException e)
        {
            System.out.println(
                "Failed to find value for argument: \"" + e.getMissingArgument().getName() + "\" for command: \"" +
                    e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (IllegalValueException e)
        {
            System.out.println(
                "Illegal argument \"" + e.getIllegalValue() + "\" for command: \"" + e.getCommand().getName() +
                    "\"");
            e.printStackTrace();
        }
        catch (EOFException e)
        {
            System.out.println("EOFException!");
            e.printStackTrace();
        }
        catch (NoPermissionException e)
        {
            System.out.println("No permission!");
            e.printStackTrace();
        }
        catch (CommandParserException e)
        {
            System.out.println("General CommandParserException!");
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerCommand(final @NonNull ServerCommandEvent event)
    {
        if (!startsWithSuperCommand(event.getCommand()))
            return;
        event.setCancelled(true);

        // TODO: Use Exception handler.
        // This may look like code duplication (because it is), but once a proper Exception Handler is used,
        // it'll all melt away into a single line.
        try
        {
            cap.parseInput(event.getSender(), event.getCommand()).run();
        }
        catch (CommandNotFoundException e)
        {
            System.out.println("Failed to find command: " + e.getMissingCommand());
            e.printStackTrace();
        }
        catch (NonExistingArgumentException e)
        {
            System.out.println("Failed to find argument: \"" + e.getNonExistingArgument() + "\" for command: \"" +
                                   e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (MissingArgumentException e)
        {
            System.out.println(
                "Failed to find value for argument: \"" + e.getMissingArgument().getName() + "\" for command: \"" +
                    e.getCommand().getName() + "\"");
            e.printStackTrace();
        }
        catch (IllegalValueException e)
        {
            System.out.println(
                "Illegal argument \"" + e.getIllegalValue() + "\" for command: \"" + e.getCommand().getName() +
                    "\"");
            e.printStackTrace();
        }
        catch (EOFException e)
        {
            System.out.println("EOFException!");
            e.printStackTrace();
        }
        catch (NoPermissionException e)
        {
            System.out.println("No permission!");
            e.printStackTrace();
        }
        catch (CommandParserException e)
        {
            System.out.println("General CommandParserException!");
            e.printStackTrace();
        }
    }
}
