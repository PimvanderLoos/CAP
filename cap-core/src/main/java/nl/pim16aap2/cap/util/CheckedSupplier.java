package nl.pim16aap2.cap.util;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception>
{
    T get()
        throws E;
}
