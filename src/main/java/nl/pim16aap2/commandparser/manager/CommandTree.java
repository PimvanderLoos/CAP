package nl.pim16aap2.commandparser.manager;

import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CommandTree
{
    private final @NonNull Map<@NonNull String, @NonNull Command> commandMap = new HashMap<>();

    void addCommand(final @NonNull Command command)
    {
        commandMap.put(command.getName(), command);
    }

    @NonNull Optional<Command> getCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Optional.ofNullable(commandMap.get(name));
    }
}
