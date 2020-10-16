package nl.pim16aap2.commandparser.manager;

import lombok.NonNull;
import nl.pim16aap2.commandparser.command.Command;

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

    @NonNull Optional<Command> getCommand(final @NonNull String name)
    {
        return Optional.ofNullable(commandMap.get(name));
    }
}
