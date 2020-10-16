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

    protected final @NonNull Map<@NonNull String, @NonNull Command> subCommands;

    // TODO: Create classes for these.
    protected final @NonNull Map<@NonNull String, @NonNull OptionalArgument<?>> optionalArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull RepeatableArgument<? extends List<?>, ?>> repeatableArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull RequiredArgument<?>> requiredArguments = new HashMap<>();

    protected final @NonNull Map<@NonNull String, @NonNull Argument<?>> arguments = new HashMap<>();

    protected final @NonNull Consumer<@NonNull CommandResult> commandExecutor;

    @Setter
    protected boolean hidden;

    @Builder
    private Command(final @NonNull String name, final @Nullable String description, final @Nullable String summary,
                    final @Nullable @Singular List<Command> subCommands,
                    final @NonNull Consumer<@NonNull CommandResult> commandExecutor,
                    final @Nullable @Singular(ignoreNullCollections = true) List<@NonNull Argument<?>> arguments,
                    final boolean hidden)
    {
        this.name = name;
        this.description = Util.valOrDefault(description, "");
        this.summary = Util.valOrDefault(summary, "");
        this.subCommands = generateSubCommandMap(subCommands);
        this.commandExecutor = commandExecutor;
        this.hidden = hidden;
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

    public @NonNull Optional<Command> getSubCommand(final @NonNull String name)
    {
        return Optional.ofNullable(subCommands.get(name));
    }

    private static Map<@NonNull String, @NonNull Command> generateSubCommandMap(
        final @Nullable List<Command> subCommands)
    {
        if (subCommands == null)
            return Collections.emptyMap();

        final Map<@NonNull String, @NonNull Command> ret = new HashMap<>(subCommands.size());
        for (final @NonNull Command cmd : subCommands)
            ret.put(cmd.getName(), cmd);
        return ret;
    }

    private void parseArguments(final @Nullable List<@NonNull Argument<?>> arguments)
    {
        if (arguments == null)
            return;

        int requiredIndex = 0;
        for (Argument<?> argument : arguments)
        {
            if (argument instanceof OptionalArgument)
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
            else if (argument instanceof RepeatableArgument)
            {
                repeatableArguments.put(argument.getName(), (RepeatableArgument<? extends List<?>, ?>) argument);
                this.arguments.put(argument.getName(), argument); // TODO: This is dumb
            }
            else
                throw new RuntimeException(
                    "Unsupported type: " + argument.getClass().getCanonicalName());
        }
    }

//    /**
//     * Organizes {@link #requiredArguments}.
//     * <p>
//     * It makes sure that no two required arguments use the same index and that those with unassigned (i.e. negative)
//     * indices are assigned a valid index.
//     */
//    private void organizeRequiredArguments()
//    {
//        for (final @NonNull RequiredArgument<?> requiredArgument : requiredArguments)
//        {
//
//        }
//    }
//
//    /**
//     * Checks if the given index is not yet taken in the list of required arguments.
//     *
//     * @param idx     The index to check.
//     * @param compare The {@link RequiredArgument} to compare the found required arguments to. This can be used to
//     *                avoid
//     * @return True if no other arguments use this index.
//     */
//    private boolean isIndexAvailable(final @NonNull Integer idx, final @Nullable RequiredArgument<?> compare)
//    {
//        for (final @NonNull RequiredArgument<?> requiredArgument : requiredArguments)
//            if (requiredArgument.getPosition().equals(idx) && !requiredArgument.equals(compare))
//                return false;
//        return true;
//    }
//
//    /**
//     * Checks if the given index is not yet taken in the list of required arguments.
//     *
//     * @param idx The index to check.
//     * @return True if no other arguments use this index.
//     */
//    private Integer getNextAvailableIndex()
//    {
//        for (final @NonNull RequiredArgument<?> requiredArgument : requiredArguments)
//            if (requiredArgument.getPosition().equals(-1))
//                return false;
//        return true;
//    }
}
