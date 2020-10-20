package nl.pim16aap2.commandparser.util;

@FunctionalInterface
public interface CheckedConsumer<T, E extends Exception>
{
    void accept(T t)
        throws E;
}
