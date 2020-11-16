package nl.pim16aap2.cap.util;

import lombok.EqualsAndHashCode;

/**
 * Represents an object with 3 values.
 *
 * @author Pim
 */
@EqualsAndHashCode
public final class Triple<T1, T2, T3>
{
    /**
     * The first member of this triple.
     */
    public T1 first;

    /**
     * The second member of this triple.
     */
    public T2 second;

    /**
     * The third member of this triple.
     */
    public T3 third;

    public Triple(final T1 first, final T2 second, final T3 third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }
}
