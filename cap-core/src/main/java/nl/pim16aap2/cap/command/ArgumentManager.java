/*
 *  MIT License
 *
 * Copyright (c) 2020 Pim van der Loos
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nl.pim16aap2.cap.command;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.localization.Localizer;
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

    ArgumentManager(final @NonNull Localizer localizer, final @NonNull List<Argument<?>> arguments,
                    final boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
        argumentsMap = new ArgumentMap(this, localizer, arguments.size());
        argumentsList = new ArrayList<>(arguments);

        // First sort the arguments we received so they are put in the arguments map in the right order.
        argumentsList.sort(COMPARATOR);

        // Optional positional arguments are only allowed AFTER all required optional positional arguments.
        // So, in other words, we can keep adding required positional arguments until we encounter an optional one.
        boolean requiredPositionalAllowed = true;
        for (final @NonNull Argument<?> argument : argumentsList)
        {
            argumentsMap.addArgument(argument);

            final boolean required = argument.isRequired();
            if (required)
                requiredArguments.add(argument);
            else
                optionalArguments.add(argument);

            if (argument.isPositional())
            {
                if (required && !requiredPositionalAllowed)
                    throw new IllegalArgumentException("Trying to add an optional positional argument before " +
                                                           "the last required positional argument! This is " +
                                                           "not supported: All optional positional arguments " +
                                                           "must come AFTER all required optional " +
                                                           "positional arguments!");
                if (!required)
                    requiredPositionalAllowed = false;
                positionalArguments.add(argument);
            }
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
     * Gets an argument from its name. See {@link Argument#getShortName(Localizer, Locale)}.
     *
     * @param argumentName  The name of the {@link Argument}.
     * @param commandSender The {@link ICommandSender} for which to get the {@link Command}. See {@link
     *                      ICommandSender#getLocale()}.
     * @return The {@link Argument}, if one is registered by the provided name.
     */
    public @NonNull Optional<Argument<?>> getArgument(final @Nullable String argumentName,
                                                      final @NonNull ICommandSender commandSender)
    {
        return getArgument(argumentName, commandSender.getLocale());
    }

    /**
     * Gets an argument from its name. See {@link Argument#getShortName(Localizer, Locale)}.
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

        protected ArgumentMap(final @NonNull ArgumentManager argumentManager, final @NonNull Localizer localizer,
                              final int initialCapacity)
        {
            super(localizer, initialCapacity);
            this.argumentManager = argumentManager;
        }

        protected ArgumentMap(final @NonNull ArgumentManager argumentManager, final @NonNull Localizer localizer)
        {
            super(localizer);
            this.argumentManager = argumentManager;
        }

        /**
         * Adds the provided command for every locale.
         *
         * @param argument The {@link Argument} to register.
         */
        public void addArgument(final @NonNull Argument<?> argument)
        {
            addEntry(argument::getShortName, argument, argumentManager::getArgumentNameCaseCheck);
            if (argument.getLongName(localizer, null) != null)
                addEntry(argument::getLongName, argument, argumentManager::getArgumentNameCaseCheck);
        }

        /**
         * Gets a {@link Argument} from its name.
         *
         * @param name   The name of the {@link Argument}. See {@link Argument#getShortName(Localizer, Locale)} and
         *               {@link Argument#getLongName(Localizer, Locale)}.
         * @param locale The {@link Locale} for which to get the {@link Argument}.
         * @return The {@link Argument} with the given name, if it is registered.
         */
        public @NonNull Optional<Argument<?>> getArgument(final @Nullable String name, final @Nullable Locale locale)
        {
            return getEntry(name, locale, argumentManager::getArgumentNameCaseCheck);
        }
    }
}
