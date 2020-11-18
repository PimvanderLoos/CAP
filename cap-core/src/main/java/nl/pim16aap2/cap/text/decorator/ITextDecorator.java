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

package nl.pim16aap2.cap.text.decorator;

import lombok.NonNull;
import nl.pim16aap2.cap.text.TextComponent;

/**
 * Represents a decorator for a piece of text that is more involved than a simple {@link TextComponent}.
 *
 * @author Pim
 */
public interface ITextDecorator
{
    /**
     * Gets the start idx of the part of text to decorate.
     *
     * @return The start idx of the part of text to decorate.
     */
    int getStart();

    /**
     * Gets the end idx of the part of text to decorate.
     *
     * @return The end idx of the part of text to decorate.
     */
    int getEnd();

    /**
     * Gets the length of the part of text to decorate.
     *
     * @return The length of the part of text to decorate.
     */
    default int getLength()
    {
        return getEnd() - getStart();
    }

    /**
     * Updates the start index.
     *
     * @param start The new start index.
     * @return This {@link ITextDecorator} instance.
     */
    @NonNull ITextDecorator setStart(final int start);

    /**
     * Updates the end index.
     *
     * @param end The new end index.
     * @return This {@link ITextDecorator} instance.
     */
    @NonNull ITextDecorator setEnd(final int end);

    /**
     * Moves the start and end indices of the part of text to decorate by a number of characters.
     *
     * @param dist The number of characters to shift.
     * @return This {@link ITextDecorator} instance.
     */
    @NonNull ITextDecorator shift(final int dist);

    /**
     * Duplicates this {@link ITextDecorator}.
     *
     * @return The duplicate of this {@link ITextDecorator}.
     */
    @NonNull ITextDecorator duplicate();
}
