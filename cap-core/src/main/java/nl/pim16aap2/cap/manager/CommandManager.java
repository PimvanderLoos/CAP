package nl.pim16aap2.cap.manager;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.command.CommandResult;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CommandNotFoundException;
import nl.pim16aap2.cap.exception.CommandParserException;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.MissingArgumentException;
import nl.pim16aap2.cap.exception.NoPermissionException;
import nl.pim16aap2.cap.exception.NonExistingArgumentException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandManager
{
    private final @NonNull Map<@NonNull String, @NonNull Command> commandMap = new HashMap<>();

    @Getter
    private final @NonNull DefaultHelpCommandRenderer helpCommandRenderer;

    /**
     * Whether or not to enable debug mode. In debug mode, {@link CommandParserException}s will generate stacktraces,
     * when it is disable, they won't (this is much faster).
     */
    @Getter
    @Setter
    protected boolean debug;

    @Builder(toBuilder = true)
    private CommandManager(final @Nullable DefaultHelpCommandRenderer helpCommandRenderer, final boolean debug)
    {
        this.helpCommandRenderer = Util.valOrDefault(helpCommandRenderer, DefaultHelpCommandRenderer::getDefault);
        this.debug = debug;
    }

    /**
     * Gets a new instance of this {@link CommandManager} using the default values.
     * <p>
     * Use {@link CommandManager#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link CommandManager}.
     */
    public static @NonNull CommandManager getDefault()
    {
        return CommandManager.builder().build();
    }

    /**
     * Parses a string containing multiple arguments delimited by spaces.
     * <p>
     * See {@link #parseInput(ICommandSender, String...)}.
     *
     * @param commandSender The {@link ICommandSender} that issued a command.
     * @param args          A single String containing multiple arguments.
     */
    public @NonNull CommandResult parseInput(final @NonNull ICommandSender commandSender, String args)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, EOFException,
               NoPermissionException, ValidationFailureException, IllegalValueException
    {
        return parseInput(commandSender, args.split(" "));
    }

    /**
     * Parses an array of commandline arguments.
     *
     * @param commandSender The {@link ICommandSender} that issued a command.
     * @param args          The commandline arguments to parse split by spaces.
     *                      <p>
     *                      If spaces are required in a value, use double quotation marks. Quotation marks that are not
     *                      escaped will be removed.
     * @return The {@link CommandResult} containing the parsed arguments.
     *
     * @throws CommandNotFoundException     If a specified command could not be found.
     * @throws NonExistingArgumentException If a specified argument could not be found. E.g. '-p=player' for a command
     *                                      for which no argument named "p" was registered.
     * @throws MissingArgumentException     If a required argument was not provided.
     * @throws EOFException                 If there are unmatched quotation marks. E.g. '-p="player'. Note that the
     *                                      matching quotation mark can be in another string further down the array.
     */
    public @NonNull CommandResult parseInput(final @NonNull ICommandSender commandSender, String... args)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, EOFException,
               NoPermissionException, ValidationFailureException, IllegalValueException
    {
        return new CommandParser(this, commandSender, args).parse();
    }

    public @NonNull CommandManager addCommand(final @NonNull Command command)
    {
        commandMap.put(command.getName(), command);
        return this;
    }

    public @NonNull Optional<Command> getCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Optional.ofNullable(commandMap.get(name));
    }

    public @NonNull Collection<@NonNull Command> getCommands()
    {
        return commandMap.values();
    }

    public List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender, final @NonNull String args)
    {
        return getTabCompleteOptions(commandSender, args.split(" "));
    }

    public List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender, final @NonNull String[] args)
    {
        try
        {
            return new CommandParser(this, commandSender, args).getTabCompleteOptions();
        }
        catch (EOFException e)
        {
            return new ArrayList<>(0);
        }
    }
}