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
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents the base class for all user-oriented exceptions.
 * <p>
 * All exceptions that subclass this exception are related to user input and serve to inform the user about what issue
 * occurred. The {@link #localizedMessage} therefore contains the error message the {@link ICommandSender} will receive
 * when the exception occurred (localized for them).
 *
 * @author Pim
 */
public class CAPException extends Exception
{
    protected final boolean stacktraceEnabled;

    /**
     * The localized error message (localized for the {@link ICommandSender} that sent the command causing this
     * exception).
     */
    @Getter
    protected final @NonNull String localizedMessage;

    public CAPException(final @NonNull String localizedMessage, final boolean stacktraceEnabled)
    {
        this.localizedMessage = localizedMessage;
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    public CAPException(final @NonNull String localizedMessage, String message, final boolean stacktraceEnabled)
    {
        super(message);
        this.localizedMessage = localizedMessage;
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    public CAPException(final @NonNull String localizedMessage, String message, Throwable cause,
                        final boolean stacktraceEnabled)
    {
        super(message, cause);
        this.localizedMessage = localizedMessage;
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    public CAPException(final @NonNull String localizedMessage, Throwable cause, final boolean stacktraceEnabled)
    {
        super(cause);
        this.localizedMessage = localizedMessage;
        this.stacktraceEnabled = stacktraceEnabled;
        fillInOptionalStackTrace();
    }

    protected CAPException(final @NonNull String localizedMessage, String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
        this.localizedMessage = localizedMessage;
        stacktraceEnabled = writableStackTrace;
        fillInOptionalStackTrace();
    }

    private void fillInOptionalStackTrace()
    {
        if (!stacktraceEnabled)
            return;
        super.fillInStackTrace();
    }

    @Override
    public Throwable fillInStackTrace()
    {
        return null;
    }
}
