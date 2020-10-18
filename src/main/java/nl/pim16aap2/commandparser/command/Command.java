package nl.pim16aap2.commandparser.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.Singular;
import nl.pim16aap2.commandparser.argument.Argument;
import nl.pim16aap2.commandparser.argument.OptionalArgument;
import nl.pim16aap2.commandparser.argument.RepeatableArgument;
import nl.pim16aap2.commandparser.argument.RequiredArgument;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Getter
public class Command
{
    protected final @NonNull String name;

    protected @NonNull String description;

    protected @NonNull String summary;

    protected final @NonNull List<@NonNull Command> subCommands;

    // TODO: Create classes for these.
    protected final @NonNull Map<@NonNull String, @NonNull OptionalArgument<?>> optionalArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull RepeatableArgument<? extends List<?>, ?>> repeatableArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull RequiredArgument<?>> requiredArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull Argument<?>> arguments = new HashMap<>();

    protected final @NonNull Consumer<@NonNull CommandResult> commandExecutor;

    protected final @NonNull String header;

    /**
     * The {@link CommandManager} that manages this command.
     */
    protected final @NonNull CommandManager commandManager;

    @Getter
    private Optional<Command> superCommand = Optional.empty();

    @Setter
    protected boolean hidden;

    @Builder
    private Command(final @NonNull String name, final @Nullable String description, final @Nullable String summary,
                    final @Nullable @Singular List<Command> subCommands,
                    final @NonNull Consumer<@NonNull CommandResult> commandExecutor,
                    final @Nullable @Singular(ignoreNullCollections = true) List<@NonNull Argument<?>> arguments,
                    final boolean hidden, final @Nullable String header, final @NonNull CommandManager commandManager)
    {
        this.name = name;
        this.description = Util.valOrDefault(description, "");
        this.summary = Util.valOrDefault(summary, "");
        this.header = Util.valOrDefault(header, "");
        this.subCommands = Util.valOrDefault(subCommands, Collections.emptyList());
        this.commandExecutor = commandExecutor;
        this.hidden = hidden;
        this.subCommands.forEach(subCommand -> subCommand.superCommand = Optional.of(this));
        this.commandManager = commandManager;
        parseArguments(arguments);
    }

    public @NonNull Optional<RequiredArgument<?>> getRequiredArgument(final @NonNull String name)
    {
        return Optional.ofNullable(requiredArguments.get(name));
    }

    public @NonNull Optional<RequiredArgument<?>> getRequiredArgumentFromIdx(final @NonNull Integer idx)
    {
        for (final @NonNull RequiredArgument<?> requiredArgument : requiredArguments.values())
            if (requiredArgument.getPosition().equals(idx))
                return Optional.of(requiredArgument);
        return Optional.empty();
    }

    public @NonNull Optional<OptionalArgument<?>> getOptionalArgument(final @NonNull String name)
    {
        return Optional.ofNullable(optionalArguments.get(name));
    }

    public @NonNull Optional<Argument<?>> getArgument(final @NonNull String name)
    {
        return Optional.ofNullable(arguments.get(name));
    }

    public @NonNull Optional<Command> getSubCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Util.searchIterable(subCommands, (val) -> val.getName().equals(name));
    }

    private void parseArguments(final @Nullable List<@NonNull Argument<?>> arguments)
    {
        if (arguments == null)
            return;

        int requiredIndex = 0;
        for (Argument<?> argument : arguments)
        {
            if (argument instanceof RepeatableArgument)
            {
                repeatableArguments.put(argument.getName(), (RepeatableArgument<? extends List<?>, ?>) argument);
                this.arguments.put(argument.getName(), argument); // TODO: This is dumb
            }
            else if (argument instanceof OptionalArgument)
            {
                optionalArguments.put(argument.getName(), (OptionalArgument<?>) argument);
                this.arguments.put(argument.getName(), argument); // TODO: This is dumb
            }
            else if (argument instanceof RequiredArgument)
            {
                final RequiredArgument<?> requiredArgument = (RequiredArgument<?>) argument;
                requiredArguments.put(argument.getName(), requiredArgument);
                this.arguments.put(argument.getName(), argument); // TODO: This is dumb
                requiredArgument.setPosition(requiredIndex);
                requiredIndex++;
            }
            else
                throw new RuntimeException(
                    "Unsupported type: " + argument.getClass().getCanonicalName());
        }
    }
}
