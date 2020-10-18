package nl.pim16aap2.commandparser.manager;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.pim16aap2.commandparser.command.Command;
import nl.pim16aap2.commandparser.command.CommandResult;
import nl.pim16aap2.commandparser.exception.CommandNotFoundException;
import nl.pim16aap2.commandparser.exception.MissingArgumentException;
import nl.pim16aap2.commandparser.exception.NonExistingArgumentException;
import nl.pim16aap2.commandparser.renderer.ColorScheme;
import nl.pim16aap2.commandparser.renderer.Text;
import org.jetbrains.annotations.Nullable;

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

    @Getter
    private final @NonNull Supplier<ColorScheme> colorScheme;

    public @NonNull CommandResult parseCommand(String... cmd)
        throws CommandNotFoundException, NonExistingArgumentException, MissingArgumentException
    {
        return new CommandParser(this, cmd).parse();
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
