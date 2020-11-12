package nl.pim16aap2.cap.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.util.LocalizedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents the set of {@link Argument}s that are part of a {@link Command}.
 *
 * @author Pim
 */
public class ArgumentManager
{
    /**
     * An (unsorted) map containing all {@link Argument}s, with their {@link Argument#getIdentifier()} as key.
     */
    protected final @NonNull ArgumentMap argumentsMap;

    /**
     * A list of all {@link Argument}s, sorted by {@link #COMPARATOR}.
     */
    protected final @NonNull List<@NonNull Argument<?>> argumentsList;

    /**
     * A list of required {@link Argument}s.
     */
    @Getter
    protected final @NonNull List<@NonNull Argument<?>> requiredArguments = new ArrayList<>(0);

    /**
     * A list of positional {@link Argument}s. The position depends on insertion order.
     */
    @Getter
    protected final @NonNull List<@NonNull Argument<?>> positionalArguments = new ArrayList<>(0);

    /**
     * A list of optional {@link Argument}s.
     */
    @Getter
    protected final @NonNull ArrayList<@NonNull Argument<?>> optionalArguments = new ArrayList<>(0);

    /**
     * Whether to enable case sensitivity or not.
     */
    protected final boolean caseSensitive;

    ArgumentManager(final @NonNull CAP cap, final @NonNull List<Argument<?>> arguments, final boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
        argumentsMap = new ArgumentMap(this, cap, arguments.size());
        argumentsList = new ArrayList<>(arguments);

        // First sort the arguments we received so they are put in the arguments map in the right order.
        argumentsList.sort(COMPARATOR);

        for (final @NonNull Argument<?> argument : argumentsList)
        {
            argumentsMap.addArgument(argument);

            if (argument.isRequired())
                requiredArguments.add(argument);
            else
                optionalArguments.add(argument);

            if (argument.isPositional())
                positionalArguments.add(argument);
        }
    }

    /**
     * Converts the name of a {@link Argument} to lower case if needed (i.e. when {@link #caseSensitive} is disabled).
     * <p>
     * When it is not needed, the same value is returned.
     *
     * @param argumentName The name of the {@link Argument}.
     * @return The name of the command, made all lower case if needed.
     */
    @Contract("!null -> !null")
    protected String getArgumentNameCaseCheck(final @Nullable String argumentName)
    {
        if (argumentName == null)
            return null;
        return caseSensitive ? argumentName : argumentName.toLowerCase();
    }

    /**
     * Gets an argument from its name. See {@link Argument#getShortNameKey()}.
     *
     * @param argumentName The name of the {@link Argument}.
     * @return The {@link Argument}, if one is registered by the provided name.
     */
    public @NonNull Optional<Argument<?>> getArgument(final @Nullable String argumentName)
    {
        return getArgument(argumentName, null);
    }

    /**
     * Gets an argument from its name. See {@link Argument#getShortNameKey()}.
     *
     * @param argumentName The name of the {@link Argument}.
     * @param locale       The {@link Locale} for which to get the {@link Command}.
     * @return The {@link Argument}, if one is registered by the provided name.
     */
    public @NonNull Optional<Argument<?>> getArgument(final @Nullable String argumentName,
                                                      final @Nullable Locale locale)
    {
        return argumentsMap.getArgument(argumentName, locale);
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

    private static final class ArgumentMap extends LocalizedMap<Argument<?>>
    {
        protected final @NonNull ArgumentManager argumentManager;

        protected ArgumentMap(final @NonNull ArgumentManager argumentManager, final @NonNull CAP cap,
                              final int initialCapacity)
        {
            super(cap, initialCapacity);
            this.argumentManager = argumentManager;
        }

        protected ArgumentMap(final @NonNull ArgumentManager argumentManager, final @NonNull CAP cap)
        {
            super(cap);
            this.argumentManager = argumentManager;
        }

        /**
         * Adds the provided command for every locale.
         *
         * @param argument The {@link Argument} to register.
         */
        public void addArgument(final @NonNull Argument<?> argument)
        {
            addEntry(argument.getShortNameKey(), argument, argumentManager::getArgumentNameCaseCheck);
            if (argument.getLongNameKey() != null)
                addEntry(argument.getLongNameKey(), argument, argumentManager::getArgumentNameCaseCheck);
        }

        /**
         * Adds the provided command for every locale.
         *
         * @param name     The (long or short) name to use as the key for the {@link Argument}.
         * @param argument The {@link Argument} to register.
         */
        public void addEntry(final @NonNull String name, final @NonNull Argument<?> argument)
        {
            addEntry(name, argument, argumentManager::getArgumentNameCaseCheck);
        }

        /**
         * Gets a {@link Argument} from its name.
         *
         * @param name   The name of the {@link Argument}. See {@link Argument#getShortNameKey()} and {@link
         *               Argument#getLongNameKey()}.
         * @param locale The {@link Locale} for which to get the {@link Argument}.
         * @return The {@link Argument} with the given name, if it is registered.
         */
        public @NonNull Optional<Argument<?>> getArgument(@Nullable String name, @Nullable Locale locale)
        {
            return getEntry(name, locale, argumentManager::getArgumentNameCaseCheck);
        }
    }
}
