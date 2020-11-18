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
 * Represents a validator for maximum values. This can be used to set a lower limit for numerical input arguments.
 *
 * @param <T> The type of the number.
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class MaximumValidator<T extends Number> implements IArgumentValidator<T>
{
    private final @NonNull RangeValidator<T> rangeValidator;

    /**
     * Gets a maximum validator for integer values.
     *
     * @param maximum The lower limit (inclusive!)
     * @return A new {@link MaximumValidator} for integer values.
     */
    public static @NonNull MaximumValidator<Integer> integerMaximumValidator(final int maximum)
    {
        return new MaximumValidator<>(RangeValidator.integerRangeValidator(maximum, maximum));
    }

    /**
     * Gets a maximum validator for integer values that uses {@link RangeValidator.ValueRequest}s to obtain the limit.
     *
     * @param maximumRequester The function to use to retrieve the maximum value.
     * @return A new {@link RangeValidator} for integer values.
     */
    public static @NonNull MaximumValidator<Integer> integerMaximumValidator(
        final @NonNull RangeValidator.ValueRequest<Integer> maximumRequester)
    {
        return new MaximumValidator<>(RangeValidator.integerRangeValidator(maximumRequester, maximumRequester));
    }

    /**
     * Gets a maximum validator for double values.
     *
     * @param maximum The lower limit (inclusive!)
     * @return A new {@link MaximumValidator} for double values.
     */
    public static @NonNull MaximumValidator<Double> doubleMaximumValidator(final double maximum)
    {
        return new MaximumValidator<>(RangeValidator.doubleRangeValidator(maximum, maximum));
    }

    /**
     * Gets a maximum validator for double values that uses {@link RangeValidator.ValueRequest}s to obtain the limit.
     *
     * @param maximumRequester The function to use to retrieve the maximum value.
     * @return A new {@link RangeValidator} for double values.
     */
    public static @NonNull MaximumValidator<Double> doubleMaximumValidator(
        final @NonNull RangeValidator.ValueRequest<Double> maximumRequester)
    {
        return new MaximumValidator<>(RangeValidator.doubleRangeValidator(maximumRequester, maximumRequester));
    }

    @Override
    public void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                         final @NonNull Argument<?> argument, final @Nullable T input)
        throws ValidationFailureException
    {
        final @NonNull T max = rangeValidator.getMax(cap, commandSender, argument);
        if (input == null || !rangeValidator.lessThanMax(cap, commandSender, argument, max, input))
        {
            final @NonNull String localizedMessage = MessageFormat
                .format(cap.getLocalizer().getMessage("error.validation.maximum", commandSender), input, max);
            throw new ValidationFailureException(argument, input == null ? "NULL" : input.toString(), localizedMessage,
                                                 cap.isDebug());
        }
    }
}
