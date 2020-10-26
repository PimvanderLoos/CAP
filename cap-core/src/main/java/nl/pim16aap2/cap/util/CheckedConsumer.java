package nl.pim16aap2.cap.util;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception>
{
    void accept(T t)
        throws E;
}
