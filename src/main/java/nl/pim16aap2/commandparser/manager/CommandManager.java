package nl.pim16aap2.commandparser.manager;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.command.CommandResult;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.text.ColorScheme;
import nl.pim16aap2.commandparser.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class CommandManager
{
    private final @NonNull Map<@NonNull String, @NonNull Command> commandMap = new HashMap<>();

    @Getter
    private final @NonNull Consumer<Text> textConsumer;

    private final @NonNull Supplier<@NonNull ColorScheme> colorScheme;

    /**
     * Parses a string containing multiple arguments delimited by spaces.
     * <p>
     * See {@link #parseCommand(String...)}.
     *
     * @param args A single String containing multiple arguments.
     */
    public @NonNull CommandResult parseCommand(String args)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, EOFException
    {
        return parseCommand(args.split(" "));
    }

    /**
     * Gets the {@link ColorScheme} registered for this {@link CommandManager} using {@link #colorScheme}.
     *
     * @return The {@link ColorScheme} obtained from the provided consumer.
     */
    public @NonNull ColorScheme getColorScheme()
    {
        return this.colorScheme.get();
    }

    /**
     * Parses an array of commandline arguments.
     *
     * @param args The commandline arguments to parse split by spaces.
     *             <p>
     *             If spaces are required in a value, use double quotation marks. Quotation marks that are not escaped
     *             will be removed.
     * @return The {@link CommandResult} containing the parsed arguments.
     *
     * @throws CommandNotFoundException     If a specified command could not be found.
     * @throws NonExistingArgumentException If a specified argument could not be found. E.g. '-p=player' for a command
     *                                      for which no argument named "p" was registered.
     * @throws MissingArgumentException     If a required argument was not provided.
     * @throws EOFException                 If there are unmatched quotation marks. E.g. '-p="player'. Note that the
     *                                      matching quotation mark can be in another string further down the array.
     */
    public @NonNull CommandResult parseCommand(String... args)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException, EOFException
    {
        return new CommandParser(this, args).parse();
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
}
