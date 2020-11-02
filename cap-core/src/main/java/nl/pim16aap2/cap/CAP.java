package nl.pim16aap2.cap;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
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
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * The main class of this library. All commands within a single command system should be registered here.
 *
 * @author Pim
 */
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CAP
{
    /**
     * The map containing all registered commands, with their names as key.
     */
    protected final @NonNull Map<@NonNull String, @NonNull Command> commandMap = new HashMap<>();

    /**
     * The map containing all registered super commands (i.e. commands without a supercommand of their own), with their
     * names as key.
     */
    @Getter
    protected final @NonNull Map<@NonNull String, @NonNull Command> superCommandMap = new HashMap<>();

    /**
     * The {@link DefaultHelpCommandRenderer} to use to render help messages.
     */
    @Getter
    @Builder.Default
    protected @NonNull final DefaultHelpCommandRenderer helpCommandRenderer = DefaultHelpCommandRenderer.getDefault();

    /**
     * Whether or not to enable debug mode. In debug mode, {@link CommandParserException}s will generate stacktraces,
     * when it is disabled, they won't (this is much faster).
     */
    @Getter
    @Setter
    @Builder.Default
    protected boolean debug = false;

    /**
     * A positive lookbehind for spaces.
     * <p>
     * When simply splitting on spaces, whitespace won't be preserved. This regex will make sure to add any trailing
     * whitespace to each argument.
     * <p>
     * For example, "/mycommand test   a" will become ["mycommand ", "test   ", "a"].
     */
    private static final @NonNull Pattern pat = Pattern.compile("((?<= ))");

    /**
     * Gets a new instance of this {@link CAP} using the default values.
     * <p>
     * Use {@link CAP#toBuilder()} if you wish to customize it.
     *
     * @return A new instance of this {@link CAP}.
     */
    public static @NonNull CAP getDefault()
    {
        return CAP.builder().build();
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
        return parseInput(commandSender, pat.split(args));
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

    /**
     * Registers a {@link Command} with this {@link CAP}.
     *
     * @param command The {@link Command} to register.
     * @return The current instance of this {@link CAP}.
     */
    public @NonNull CAP addCommand(final @NonNull Command command)
    {
        commandMap.put(command.getName(), command);
        if (!command.getSuperCommand().isPresent())
            superCommandMap.put(command.getName(), command);
        return this;
    }

    /**
     * Gets a {@link Command} from its name.
     *
     * @param name The name of the {@link Command}. See {@link Command#getName()}.
     * @return The {@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Optional.ofNullable(commandMap.get(name));
    }

    /**
     * Gets a super{@link Command} from its name (i.e. a {@link Command} without any supers of its own).
     *
     * @param name The name of the super{@link Command}. See {@link Command#getName()}.
     * @return The super{@link Command#getName()} with the given name, if it is registered in the {@link CAP}.
     */
    public @NonNull Optional<Command> getSuperCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Optional.ofNullable(superCommandMap.get(name));
    }

    /**
     * Gets all the {@link Command}s registered in this {@link CAP}.
     *
     * @return All the {@link Command}s registered in this {@link CAP}.
     */
    public @NonNull Collection<@NonNull Command> getCommands()
    {
        return commandMap.values();
    }

    /**
     * Gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param args          The current set of (potentially incomplete) input arguments.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender, final @NonNull String args)
    {
        return getTabCompleteOptions(commandSender, args.split(" "));
    }

    /**
     * Gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param args          The current set of (potentially incomplete) input arguments split on spaces.
     * @return The list of suggestions based on the current set of input arguments.
     */
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
