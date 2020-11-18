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

package nl.pim16aap2.cap.argument.validator.number;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.argument.validator.IArgumentValidator;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * Represents a validator for minimum values. This can be used to set a lower limit for numerical input arguments.
 *
 * @param <T> The type of the number.
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MinimumValidator<T extends Number> implements IArgumentValidator<T>
{
    private final @NonNull RangeValidator<T> rangeValidator;

    /**
     * Gets a minimum validator for integer values.
     *
     * @param minimum The lower limit (inclusive!)
     * @return A new {@link MinimumValidator} for integer values.
     */
    public static @NonNull MinimumValidator<Integer> integerMinimumValidator(final int minimum)
    {
        return new MinimumValidator<>(RangeValidator.integerRangeValidator(minimum, minimum));
    }

    /**
     * Gets a minimum validator for integer values that uses {@link RangeValidator.ValueRequest}s to obtain the limit.
     *
     * @param minimumRequester The function to use to retrieve the minimum value.
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull MinimumValidator<Integer> integerMinimumValidator(
        final @NonNull RangeValidator.ValueRequest<Integer> minimumRequester)
    {
        return new MinimumValidator<>(RangeValidator.integerRangeValidator(minimumRequester, minimumRequester));
    }

    /**
     * Gets a minimum validator for double values.
     *
     * @param minimum The lower limit (inclusive!)
     * @return A new {@link MinimumValidator} for double values.
     */
    public static @NonNull MinimumValidator<Double> doubleMinimumValidator(final double minimum)
    {
        return new MinimumValidator<>(RangeValidator.doubleRangeValidator(minimum, minimum));
    }

    /**
     * Gets a minimum validator for double values that uses {@link RangeValidator.ValueRequest}s to obtain the limit.
     *
     * @param minimumRequester The function to use to retrieve the minimum value.
     * @return A new {@link RangeValidator} for double values.
     */
    public static @NonNull MinimumValidator<Double> doubleMinimumValidator(
        final @NonNull RangeValidator.ValueRequest<Double> minimumRequester)
    {
        return new MinimumValidator<>(RangeValidator.doubleRangeValidator(minimumRequester, minimumRequester));
    }

    @Override
    public void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull Argument<?> argument, final @Nullable T input)
        throws ValidationFailureException
    {
        final @NonNull T min = rangeValidator.getMax(cap, commandSender, argument);
        if (input == null || !rangeValidator.moreThanMin(cap, commandSender, argument, min, input))
        {
            final @NonNull String localizedMessage = MessageFormat
                .format(cap.getLocalizer().getMessage("error.validation.minimum", commandSender), input, min);
            throw new ValidationFailureException(argument, input == null ? "NULL" : input.toString(), localizedMessage,
                                                 cap.isDebug());
        }
    }
}
