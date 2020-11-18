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

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static nl.pim16aap2.cap.util.UtilsForTesting.*;

class MaximumValidatorTest
{
    @Test
    void validateRangeInteger()
    {
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class, () ->
            maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 11));

        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10));
        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 9));
    }

    @Test
    void validateRangeDouble()
    {
        final @NonNull MaximumValidator<Double> maximumValidator = MaximumValidator.doubleMaximumValidator(10);

        Assertions.assertThrows(ValidationFailureException.class, () ->
            maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10.1));

        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 9.9));
        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, 10.0));
    }

    /**
     * A {@link RangeValidator.ValueRequest} that returns a specific value.
     *
     * @param ret The value to return.
     * @return The specified value.
     */
    private @NonNull Integer maximumSupplier(final @NonNull Integer ret, final @NonNull CAP cap,
                                             final @NonNull ICommandSender commandSender, @NonNull Argument<?> argument)
    {
        return ret;
    }

    @Test
    void validateDynamic()
    {
        final int maximum = 10;
        final @NonNull MaximumValidator<Integer> maximumValidator = MaximumValidator.integerMaximumValidator(
            (cap1, commandSender1, argument1) -> maximumSupplier(maximum, cap1, commandSender1, argument1));

        Assertions.assertThrows(ValidationFailureException.class, () ->
            maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, maximum + 1));

        Assertions.assertDoesNotThrow(
            () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, maximum));
        Assertions
            .assertDoesNotThrow(
                () -> maximumValidator.validate(LOCALIZED_CAP, DEFAULT_COMMAND_SENDER, DUMMY_ARGUMENT, maximum - 1));
    }
}
