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
import nl.pim16aap2.commandparser.exception.CommandParserException;
import nl.pim16aap2.commandparser.manager.CommandManager;
import nl.pim16aap2.commandparser.util.CheckedConsumer;
import nl.pim16aap2.commandparser.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class Command
{
    static final @NonNull OptionalArgument<Boolean> DEFAULT_HELP_ARGUMENT = Argument.FlagArgument
        .getOptional(true).name("h").longName("help").summary("Displays the help menu for this command.").build();

    protected final @NonNull String name;

    @Setter
    protected @NonNull String description;

    @Setter
    protected @NonNull String summary;

    protected final @NonNull List<@NonNull Command> subCommands;

    // TODO: Create classes for these.
    protected final @NonNull Map<@NonNull String, @NonNull OptionalArgument<?>> optionalArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull RepeatableArgument<? extends List<?>, ?>> repeatableArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull RequiredArgument<?>> requiredArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull Argument<?>> arguments = new HashMap<>();

    protected final @NonNull CheckedConsumer<@NonNull CommandResult, CommandParserException> commandExecutor;

    @Setter
    protected @NonNull String header;

    /**
     * The {@link CommandManager} that manages this command.
     */
    protected final @NonNull CommandManager commandManager;

    private Optional<Command> superCommand = Optional.empty();

    @Setter
    protected boolean hidden;

    protected boolean addDefaultHelpArgument;

    @Builder(builderMethodName = "commandBuilder")
    protected Command(final @NonNull String name, final @Nullable String description, final @Nullable String summary,
                      final @Nullable @Singular List<Command> subCommands,
                      final @NonNull CheckedConsumer<@NonNull CommandResult, CommandParserException> commandExecutor,
                      final @Nullable @Singular(ignoreNullCollections = true) List<@NonNull Argument<?>> arguments,
                      final boolean hidden, final @Nullable String header, final @NonNull CommandManager commandManager,
                      final @Nullable Boolean addDefaultHelpArgument, final @Nullable Boolean addDefaultHelpSubCommand)
    {
        this.name = name;
        this.description = Util.valOrDefault(description, "");
        this.summary = Util.valOrDefault(summary, "");
        this.header = Util.valOrDefault(header, "");
        this.subCommands = new ArrayList<>(Util.valOrDefault(subCommands, new ArrayList<>(0)));
        this.commandExecutor = commandExecutor;
        this.hidden = hidden;
        if (addDefaultHelpSubCommand != null && addDefaultHelpSubCommand)
            this.subCommands.add(0, DefaultHelpCommand.getDefault(commandManager));
        this.subCommands.forEach(subCommand -> subCommand.superCommand = Optional.of(this));
        this.commandManager = commandManager;
        this.addDefaultHelpArgument = Util.valOrDefault(addDefaultHelpArgument, false);
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

    /**
     * Searches for a sub{@link Command} of a given types.
     *
     * @param clazz The {@link Class} to search for.
     * @param <T>   The Type of the sub{@link Command} to find.
     * @return An {@link Optional} containing the sub{@link Command}.
     */
    public @NonNull <T> Optional<Command> getSubCommand(final @NonNull Class<T> clazz)
    {
        return Util.searchIterable(subCommands, (val) -> clazz.isAssignableFrom(val.getClass()));
    }

    public @NonNull Optional<Command> getSubCommand(final @Nullable String name)
    {
        if (name == null)
            return Optional.empty();
        return Util.searchIterable(subCommands, (val) -> val.getName().equals(name));
    }

    private void parseArguments(final @Nullable List<@NonNull Argument<?>> arguments)
    {
        if (this.addDefaultHelpArgument)
        {
            optionalArguments.put(DEFAULT_HELP_ARGUMENT.getName(), DEFAULT_HELP_ARGUMENT);
            this.arguments.put(DEFAULT_HELP_ARGUMENT.getName(), DEFAULT_HELP_ARGUMENT);
        }

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
