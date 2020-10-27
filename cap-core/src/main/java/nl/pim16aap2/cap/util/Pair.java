package nl.pim16aap2.cap.util;

import lombok.EqualsAndHashCode;

/**
 * Represents a name-value pair.
 *
 * @author Pim
 */
@EqualsAndHashCode
public final class Pair<T1, T2>
{
    /**
     * The first member of this pair.
     */
    public T1 first;

    /**
     * The second member of this pair.
     */
    public T2 second;

    public Pair(final T1 first, final T2 second)
    {
        this.first = first;
        this.second = second;
    }
}
