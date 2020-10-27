package nl.pim16aap2.cap.util;

import java.util.function.Supplier;

/**
 * Represents a {@link Supplier} that can throw checked exceptions.
 *
 * @param <T> The type of results supplied by this supplier
 * @param <E> The type of the checked exception that may be thrown.
 */
@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception>
{
    T get()
        throws E;
}
