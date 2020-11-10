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
import nl.pim16aap2.cap.commandparser.CommandParser;
import nl.pim16aap2.cap.commandparser.TabCompletionSuggester;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.CAPException;
import nl.pim16aap2.cap.exception.ExceptionHandler;
import nl.pim16aap2.cap.renderer.DefaultHelpCommandRenderer;
import nl.pim16aap2.cap.util.Pair;
import nl.pim16aap2.cap.util.TabCompletionCache;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
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
     * The map containing all registered top-level commands (i.e. commands without a supercommand of their own), with
     * their names as key.
     */
    @Getter
    protected final @NonNull Map<@NonNull String, @NonNull Command> topLevelCommandMap = new HashMap<>();

    /**
     * The {@link DefaultHelpCommandRenderer} to use to render help messages.
     */
    @Getter
    @Builder.Default
    protected final @NonNull DefaultHelpCommandRenderer helpCommandRenderer = DefaultHelpCommandRenderer.getDefault();

    /**
     * Whether or not to cache tab-completion suggestions using a {@link TabCompletionCache}.
     */
    @Getter
    @Setter
    @Builder.Default
    protected boolean cacheTabCompletionSuggestions = true;

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
     * Whether to enable case sensitivity. Default: False.
     * <p>
     * When enabled, "/myCommand" will not match to "/mycommand". When disabled, this works fine.
     * <p>
     * Note that this only applies to command and argument names, not argument values.
     */
    @Getter
    @Builder.Default
    protected final boolean caseSensitive = false;

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
     *
     * @param commandSender The {@link ICommandSender} that issued a command.
     * @param input         The string that may contain a set of commands and arguments.
     * @return The {@link CommandResult} if it could be constructed.
     */
    public @NonNull Optional<CommandResult> parseInput(final @NonNull ICommandSender commandSender,
                                                       final @NonNull String input)
    {
        try
        {
            return Optional.of(new CommandParser(this, commandSender, input, separator).parse());
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
        commandMap.put(getCommandNameCaseCheck(command.getName()), command);
        if (!command.getSuperCommand().isPresent())
            topLevelCommandMap.put(getCommandNameCaseCheck(command.getName()), command);
        return this;
    }

    /**
     * Converts the name of a {@link Command} to lower case if needed (i.e. when {@link #caseSensitive} is disabled).
     * <p>
     * When it is not needed, the same value is returned.
     *
     * @param commandName The name of the {@link Command}.
     * @return The name of the command, made all lower case if needed.
     */
    @Contract("!null -> !null")
    protected String getCommandNameCaseCheck(final @Nullable String commandName)
    {
        if (commandName == null)
            return null;
        return caseSensitive ? commandName : commandName.toLowerCase();
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
        return Optional.ofNullable(commandMap.get(getCommandNameCaseCheck(name)));
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
        return Optional.ofNullable(topLevelCommandMap.get(getCommandNameCaseCheck(name)));
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
     * @param input         The current set of (potentially incomplete) input arguments.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull List<String> getTabCompleteOptions(final @NonNull ICommandSender commandSender,
                                                       final @NonNull String input)
    {
        final @NonNull TabCompletionSuggester suggester =
            new TabCompletionSuggester(this, commandSender, input, separator);
        final @NonNull Supplier<List<String>> supplier = () -> suggester.getTabCompleteOptions(false);

        if (!cacheTabCompletionSuggestions)
            return supplier.get();

        final @NonNull Pair<@NonNull String, @NonNull String> lastArgument = suggester.getLastArgumentData();

        return tabCompletionCache.getTabCompleteOptions(commandSender, suggester.getArgs(),
                                                        lastArgument.first + lastArgument.second, supplier,
                                                        suggester.isOpenEnded());
    }

    /**
     * Asynchronously gets a list of suggestions for tab complete based on the current set of arguments.
     *
     * @param commandSender The {@link ICommandSender} to get the suggestions for.
     * @param input         The current set of (potentially incomplete) input arguments.
     * @return The list of suggestions based on the current set of input arguments.
     */
    public @NonNull CompletableFuture<List<String>> getTabCompleteOptionsAsync(
        final @NonNull ICommandSender commandSender, final @NonNull String input)
    {
        final @NonNull TabCompletionSuggester suggester =
            new TabCompletionSuggester(this, commandSender, input, separator);
        final @NonNull Supplier<List<String>> supplier = () -> suggester.getTabCompleteOptions(true);

        if (!cacheTabCompletionSuggestions)
            return CompletableFuture.supplyAsync(supplier);

        final @NonNull Pair<@NonNull String, @NonNull String> lastArgument = suggester.getLastArgumentData();

        return tabCompletionCache.getTabCompleteOptionsAsync(commandSender, suggester.getArgs(),
                                                             lastArgument.first + lastArgument.second, supplier,
                                                             suggester.isOpenEnded());
    }
}
