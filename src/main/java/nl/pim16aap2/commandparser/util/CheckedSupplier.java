package nl.pim16aap2.commandparser.util;

@FunctionalInterface
public interface CheckedSupplier<T, E extends Exception>
{
    T get()
        throws E;
}
