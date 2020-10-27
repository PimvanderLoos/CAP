package nl.pim16aap2.cap.command;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class ArgumentManager
{
    /**
     * An (unsorted) map containing all {@link Argument}s, with their {@link Argument#getName()} as key.
     */
    protected final @NonNull Map<@NonNull String, @NonNull Argument<?>> argumentsMap;

    /**
     * A list of all {@link Argument}s, sorted by {@link #COMPARATOR}.
     */
    @Getter(AccessLevel.PRIVATE)
    protected final @NonNull List<@NonNull Argument<?>> argumentsList;

    /**
     * A list of required {@link Argument}s.
     */
    protected final @NonNull List<@NonNull Argument<?>> requiredArguments = new ArrayList<>(0);

    /**
     * A list of positional {@link Argument}s. The position depends on insertion order.
     */
    protected final @NonNull List<@NonNull Argument<?>> positionalArguments = new ArrayList<>(0);

    /**
     * A list of optional {@link Argument}s.
     */
    protected final @NonNull ArrayList<@NonNull Argument<?>> optionalArguments = new ArrayList<>(0);

    ArgumentManager(final @NonNull List<Argument<?>> arguments)
    {
        argumentsMap = new HashMap<>(arguments.size());
        argumentsList = new ArrayList<>(arguments);

        // First sort the arguments we received so they are put in the arguments map in the right order.
        argumentsList.sort(COMPARATOR);

        for (final @NonNull Argument<?> argument : argumentsList)
        {
            argumentsMap.put(argument.getName(), argument);
            if (argument.getLongName() != null)
                argumentsMap.put(argument.getLongName(), argument);

            if (argument.isRequired())
                requiredArguments.add(argument);
            else
                optionalArguments.add(argument);

            if (argument.isPositional())
                positionalArguments.add(argument);
        }
    }

    /**
     * Gets an argument from its name. See {@link Argument#getName()}.
     *
     * @param argumentName The name of the {@link Argument}.
     * @return The {@link Argument}, if one is registered by the provided name.
     */
    public @NonNull Optional<Argument<?>> getArgument(final @Nullable String argumentName)
    {
        return Optional.ofNullable(argumentsMap.get(argumentName));
    }

    /**
     * Gets a positional argument from its index. See {@link #positionalArguments}.
     *
     * @param idx The index of the {@link Argument}.
     * @return The {@link Argument}, if one exists at the provided index.
     */
    public @NonNull Optional<Argument<?>> getPositionalArgumentAtIdx(final int idx)
    {
        if (idx >= positionalArguments.size())
            return Optional.empty();
        return Optional.of(positionalArguments.get(idx));
    }

    /**
     * Gets a list of all {@link Argument}s.
     *
     * @return A list of all {@link Argument}s.
     */
    public List<@NonNull Argument<?>> getArguments()
    {
        return argumentsList;
    }

    /**
     * The sorting used for the arguments.
     */
    private static final @NonNull Comparator<Argument<?>> COMPARATOR = (argument, t1) ->
    {
        if (argument.isPositional() != t1.isPositional())
            return argument.isPositional() ? -1 : 1;
        if (argument.isRequired() != t1.isRequired())
            return argument.isRequired() ? -1 : 1;
        if (argument.isValuesLess() != t1.isValuesLess())
            return argument.isValuesLess() ? -1 : 1;
        if (argument.isRepeatable() != t1.isRepeatable())
            return argument.isRepeatable() ? 1 : -1;
        return 0;
    };
}
