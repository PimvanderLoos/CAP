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

package nl.pim16aap2.cap.commandsender;

import lombok.NonNull;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Represents an object that sent a command and can be interacted with.
 *
 * @author Pim
 */
public interface ICommandSender
{
    /**
     * Gets the {@link Locale} for this {@link ICommandSender}.
     *
     * @return The selected {@link Locale}. (Default: null).
     */
    default @Nullable Locale getLocale()
    {
        return null;
    }

    /**
     * Sends a message to this {@link ICommandSender}.
     *
     * @param message The message to send.
     */
    void sendMessage(final @NonNull Text message);

    /**
     * Gets the {@link ColorScheme} to use for all messages send to this {@link ICommandSender}.
     *
     * @return The {@link ColorScheme} to use for all messages send to this {@link ICommandSender}.
     */
    @NonNull ColorScheme getColorScheme();
}
