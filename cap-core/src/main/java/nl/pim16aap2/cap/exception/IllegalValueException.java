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

package nl.pim16aap2.cap.exception;

import lombok.Getter;
import lombok.NonNull;
import nl.pim16aap2.cap.argument.Argument;

@Getter
public class IllegalValueException extends CAPException
{
    private final String illegalValue;
    private final @NonNull Argument<?> argument;

    public IllegalValueException(final @NonNull Argument<?> argument, final @NonNull String illegalValue,
                                 final @NonNull String localizedMessage, final boolean stacktrace)
    {
        super(localizedMessage,
              "Received illegal value \"" + illegalValue + "\" for argument: " + argument.getIdentifier(), stacktrace);
        this.illegalValue = illegalValue;
        this.argument = argument;
    }
}

