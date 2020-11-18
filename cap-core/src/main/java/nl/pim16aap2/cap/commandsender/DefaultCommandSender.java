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

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nl.pim16aap2.cap.text.ColorScheme;
import nl.pim16aap2.cap.text.Text;

/**
 * Represents a basic implementation of {@link ICommandSender}.
 * <p>
 * It has access to all commands and sends plain text messages to stdout.
 *
 * @author Pim
 */
public class DefaultCommandSender implements ICommandSender
{
    @Setter
    @Getter
    protected ColorScheme colorScheme = ColorScheme.builder().build();

    @Override
    public void sendMessage(final @NonNull Text message)
    {
        System.out.println(message);
    }

    @Override
    public int hashCode()
    {
        // A hashcode of 0 works fine, because this command sender interacts directly with sysout,
        // which is assumed to be just 1 user. This is only a default implementation anyway, so
        // it doesn't matter that this may not cover every use case.
        return 0;
    }

    @Override
    public String toString()
    {
        return "Default Command Sender";
    }
}
