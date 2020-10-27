package nl.pim16aap2.cap.util.Functional;

import java.util.function.Consumer;

/**
 * Represents a {@link Consumer} that can throw checked exceptions.
 *
 * @param <T> The type of the input to the operation
 * @param <E> The type of the checked exception that may be thrown.
 */
@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception>
{
    void accept(T t)
        throws E;
}
