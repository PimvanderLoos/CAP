package nl.pim16aap2.commandparser.manager;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArgumentManager
{
    protected final @NonNull Map<@NonNull String, @NonNull Argument<?>> argumentsMap;

    protected final @NonNull List<@NonNull Argument<?>> argumentsList;

    protected final @NonNull List<@NonNull Argument<?>> requiredArguments = new ArrayList<>(0);

    protected final @NonNull List<@NonNull Argument<?>> positionalArguments = new ArrayList<>(0);

    protected final @NonNull ArrayList<@NonNull Argument<?>> optionalArguments = new ArrayList<>(0);

    public ArgumentManager(final @NonNull List<Argument<?>> arguments)
    {
        argumentsMap = new HashMap<>(arguments.size());
        argumentsList = new ArrayList<>(arguments);

        // First sort the arguments we received so they are put in the arguments map in the right order.
        argumentsList.sort(COMPARATOR);

        for (final @NonNull Argument<?> argument : argumentsList)
        {
            this.argumentsMap.put(argument.getName(), argument);
            if (argument.getLongName() != null)
                this.argumentsMap.put(argument.getLongName(), argument);

            if (argument.isRequired())
                this.requiredArguments.add(argument);
            else
                this.optionalArguments.add(argument);

            if (argument.isPositional())
                this.positionalArguments.add(argument);
        }
    }

    public @NonNull List<@NonNull Argument<?>> getRequiredArguments()
    {
        return requiredArguments;
    }

    public @NonNull Optional<Argument<?>> getArgument(final @Nullable String argumentName)
    {
        return Optional.ofNullable(argumentsMap.get(argumentName));
    }

    public @NonNull Optional<Argument<?>> getPositionalArgumentAtIdx(final int idx)
    {
        if (idx >= positionalArguments.size())
            return Optional.empty();
        return Optional.of(positionalArguments.get(idx));
    }

    public List<@NonNull Argument<?>> getArguments()
    {
        return argumentsList;
    }

    private static final Comparator<Argument<?>> COMPARATOR = (argument, t1) ->
    {
        if (argument.isPositional() && !t1.isPositional())
            return -1;
        if (argument.isRequired() && !t1.isRequired())
            return -1;
        if (argument.isValuesLess() && !t1.isValuesLess())
            return 1;
        if (argument.isRepeatable() && !t1.isRepeatable())
            return 1;
        return 0;
    };
}
