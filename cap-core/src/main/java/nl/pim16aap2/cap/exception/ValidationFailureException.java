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
import org.jetbrains.annotations.Nullable;

@Getter
public class ValidationFailureException extends CAPException
{
    private final @Nullable Argument<?> argument;
    private final @NonNull String value;

    public ValidationFailureException(final @Nullable Argument<?> argument, final @NonNull String value,
                                      final @NonNull String localizedMessage, final boolean stacktraceEnabled)
    {
        super(localizedMessage, stacktraceEnabled);
        this.argument = argument;
        this.value = value;
    }

    public ValidationFailureException(final @NonNull String value, final @NonNull String localizedMessage,
                                      final boolean stacktraceEnabled)
    {
        this(null, value, localizedMessage, stacktraceEnabled);
    }
}
