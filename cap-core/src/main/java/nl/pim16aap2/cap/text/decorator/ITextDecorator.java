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
     * @param end The new start index.
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
