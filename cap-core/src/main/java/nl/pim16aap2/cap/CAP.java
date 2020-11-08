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
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.util.Functional.CheckedSupplier;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.TabCompletionCache;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * The main class of this library. All commands within a single command system should be registered here.
 *
 * @author Pim
 */
@SuperBuilder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class CAP
{
    // Ideally, we wouldn't construct this at all if not needed, but Lombok won't allow me to use a constructor :(
    private final @NonNull TabCompletionCache tabCompletionCache = new TabCompletionCache();

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
    protected final @NonNull DefaultHelpCommandRenderer helpCommandRenderer = DefaultHelpCommandRenderer.getDefault();

    /**
     * Whether or not to cache tabcompletion suggestions using a  {@link TabCompletionCache}.
     */
    @Getter
    @Setter
    @Builder.Default
    protected boolean cacheTabcompletionSuggestions = true;

    /**
     * The {@link ExceptionHandler} that is used to handle CAP-related exceptions.
     * <p>
     * When null, all exceptions will be rethrown as {@link RuntimeException}s.
     */
    @Getter
    @Builder.Default
    protected final @Nullable ExceptionHandler exceptionHandler = ExceptionHandler.getDefault();

    /**
     * The separator between a free argument's name and it's value. In the case of "--player=pim16aap2", this would be
     * "=". Default: " ".
     */
    @Getter
    @Builder.Default
    protected final char separator = ' ';

    /**
     * Whether or not to enable debug mode. In debug mode, {@link CAPException}s will generate stacktraces, when it is
     * disabled, they won't (this is much faster).
     */
    @Getter
    @Setter
    @Builder.Default
    protected boolean debug = false;

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
     * See {@link #parseInput(ICommandSender, List)}.
     *
     * @param commandSender The {@link ICommandSender} that issued a command.
     * @param args          A single String containing multiple arguments.
     */
    public @NonNull Optional<CommandResult> parseInput(final @NonNull ICommandSender commandSender, String args)
    {
        return parseInput(commandSender, split(args));
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
     */
    public @NonNull Optional<CommandResult> parseInput(final @NonNull ICommandSender commandSender,
                                                       final @NonNull List<String> args)
    {
        try
        {
            return Optional.of(new CommandParser(this, commandSender, args, Character.toString(separator)).parse());
        }
        catch (CAPException exception)
        {
            if (exceptionHandler == null)
                throw new RuntimeException(exception);
            exceptionHandler.handleException(commandSender, exception);
        }
        // TODO: Use our own exception
        catch (EOFException exception)
        {
            throw new RuntimeException(exception);
        }
        return Optional.empty();
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
     * Splits a string containing a command on spaces while preserving whitespace as trailing whitespace.
     * <p>
     * E.g. <pre>"/mycommand  arg  value"</pre> will return <pre>["/mycommand  ", "arg  ", "value"]</pre>
     *
     * @param command The string to split.
     * @return The command split on spaces.
     */
    public static @NonNull List<String> split(final @NonNull String command)
    {
        final @NonNull List<String> args = new ArrayList<>();
        int startIdx = 0;
        boolean lastWhiteSpace = false;
        for (int idx = 0; idx < command.length(); ++idx)
        {
            final char c = command.charAt(idx);
            if (Character.isWhitespace(c))
                lastWhiteSpace = true;
            else
            {
                if (lastWhiteSpace)
                {
                    args.add(command.substring(startIdx, idx));
                    startIdx = idx;
                }
                lastWhiteSpace = false;
            }
        }
        if (startIdx < command.length())
            args.add(command.substring(startIdx));
        return args;
    }

    /**
     * Gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param args          The current set of (potentially incomplete) input arguments.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                       final @NonNull String args)
    {
        return getTabCompleteOptions(commandSender, split(args));
    }

    /**
     * Gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param args          The current set of (potentially incomplete) input arguments split on spaces.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                       final @NonNull List<String> args)
    {
        try
        {
            final @NonNull CheckedSupplier<List<String>, EOFException> supplier = () ->
                new CommandParser(this, commandSender, args, Character.toString(separator))
                    .getTabCompleteOptions(false);

            if (!cacheTabcompletionSuggestions)
                return supplier.get();

            final @NonNull Pair<@NonNull String, @NonNull String> lastArgument =
                CommandParser.getLastArgumentData(args, separator);

            return tabCompletionCache.getTabCompleteOptions(commandSender, args, lastArgument.first +
                lastArgument.second, supplier);
        }
        catch (EOFException e)
        {
            return new ArrayList<>(0);
        }
    }

    /**
     * Asynchronously gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param args          The current set of (potentially incomplete) input arguments split on spaces.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull CompletableFuture<List<String>> getTabCompleteOptionsAsync(
        final @NonNull ICommandSender commandSender, final @NonNull List<String> args)
    {
        // TODO: Do something about the EOFException.
        final @NonNull Supplier<List<String>> supplier = () ->
        {
            try
            {
                return new CommandParser(this, commandSender, args, Character.toString(separator))
                    .getTabCompleteOptions(true);
            }
            catch (EOFException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };

        if (!cacheTabcompletionSuggestions)
            return CompletableFuture.supplyAsync(supplier);

        final @NonNull Pair<@NonNull String, @NonNull String> lastArgument =
            CommandParser.getLastArgumentData(args, separator);

        return tabCompletionCache.getTabCompleteOptionsAsync(commandSender, args, lastArgument.first +
            lastArgument.second, supplier);
    }
}
