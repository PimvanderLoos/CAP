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

package nl.pim16aap2.cap.argument.validator;

import lombok.NonNull;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.commandsender.ICommandSender;
import nl.pim16aap2.cap.exception.ValidationFailureException;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a way to validation arguments. For example by making sure that integer values are in a specific range.
 *
 * @param <T> The type of the input to validate.
 * @author Pim
 */
public interface IArgumentValidator<T>
{
    /**
     * Validates input.
     *
     * @param cap           The {@link CAP} instance for which to validate the input.
     * @param commandSender The {@link ICommandSender} for which to validate the input.
     * @param argument      The {@link Argument} to validate the input for.
     * @param input         The input to validate.
     * @return True if the input is valid, otherwise false.
     *
     * @throws ValidationFailureException When the input variable is invalid.
     */
    void validate(final @NonNull CAP cap, final @NonNull ICommandSender commandSender,
                  final @NonNull Argument<?> argument, final @Nullable T input)
        throws ValidationFailureException;
}
