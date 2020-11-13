package nl.pim16aap2.cap.argument.parser;

import lombok.NonNull;

/**
 * Represents an object that can parse a String into a desired type.
 *
 * @param <T> The type of the result to parse the String into.
 * @author Pim
 */
public abstract class ArgumentParser<T>
{
    public abstract @NonNull T parseArgument(final @NonNull String value)
        throws IllegalArgumentException;
}
