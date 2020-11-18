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

package nl.pim16aap2.cap.util;

import lombok.NonNull;
import lombok.Value;
import nl.pim16aap2.cap.CAP;
import nl.pim16aap2.cap.argument.Argument;
import nl.pim16aap2.cap.command.Command;
import nl.pim16aap2.cap.commandsender.ICommandSender;

/**
 * Represents a request made for tab-completion suggestions.
 *
 * @author Pim
 */
@Value
public class TabCompletionRequest
{
    /**
     * The {@link Command} for which the tab-completion suggestions were requested.
     */
    @NonNull Command command;
    /**
     * The {@link Argument} for which the tab-completion suggestions were requested.
     */
    @NonNull Argument<?> argument;
    /**
     * The {@link ICommandSender} that requested the tab-completion suggestions.
     */
    @NonNull ICommandSender commandSender;
    /**
     * The partial String to use as base for the tab-completion suggestions.
     */
    @NonNull String partial;

    /**
     * Whether this request was made asynchronously or not.
     * <p>
     * False = This request was made from the main thread.
     * <p>
     * True = This request was made asynchronously.
     */
    boolean async;

    /**
     * The {@link CAP} instance that issued this request.
     */
    @NonNull CAP cap;
}
