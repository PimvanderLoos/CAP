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

package nl.pim16aap2.cap.argument;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.parser.ArgumentParser;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.IllegalValueException;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import nl.pim16aap2.cap.localization.ArgumentNamingSpec;
import nl.pim16aap2.cap.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RepeatableArgument<T> extends Argument<T>
{
    private static final boolean VALUE_LESS = false;
    private static final boolean REPEATABLE = true;
    private static final boolean POSITION = false;

    @Builder(builderMethodName = "repeatableBuilder")
    private RepeatableArgument(final @NonNull ArgumentNamingSpec nameSpec, final @NonNull ArgumentParser<T> parser,
                               final boolean required, final @Nullable ITabCompleteFunction tabCompleteFunction,
                               final @Nullable IArgumentValidator<T> argumentValidator,
                               final @NonNull String identifier)
    {
        super(nameSpec, parser, null, VALUE_LESS, REPEATABLE, POSITION, required, tabCompleteFunction,
              argumentValidator, identifier);
    }

    @Override
    public @NonNull IParsedArgument<?> getParsedArgument(final @Nullable String value, final @NonNull CAP cap,
                                                         final @NonNull ICommandSender commandSender)
        throws ValidationFailureException, IllegalValueException
    {
        final ParsedRepeatableArgument<T> ret = new ParsedRepeatableArgument<>();
        if (value != null)
            ret.addValue(parseArgument(value, cap, commandSender));
        return ret;
    }

    @Override
    public @NonNull IParsedArgument<?> getDefault()
    {
        return new ParsedRepeatableArgument<T>();
    }

    /**
     * Represents an {@link IParsedArgument} for {@link RepeatableArgument}s. Unlike a regular {@link ParsedArgument},
     * this one stores the results in a list.
     *
     * @param <T> The type of the parsed arguments.
     */
    public static class ParsedRepeatableArgument<T> implements IParsedArgument<List<T>>
    {
        @Getter(onMethod = @__({@Override}))
        protected @NonNull List<T> value;

        private ParsedRepeatableArgument()
        {
            value = new ArrayList<>(0);
        }

        /**
         * Adds a value to the {@link List}.
         *
         * @param newValue The new value to add.
         */
        public void addValue(final @NonNull T newValue)
        {
            value.add(newValue);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> void updateValue(final @Nullable U newValue)
        {
            if (newValue == null)
                return;
            value.addAll((List<T>) newValue);
        }

        @Override
        public String toString()
        {
            return Util.listToString(value, Objects::toString);
        }
    }
}
