package nl.pim16aap2.cap.argument.parser;

import lombok.NonNull;

public abstract class ArgumentParser<T>
{
    public abstract @NonNull T parseArgument(final @NonNull String value)
        throws IllegalArgumentException;
}
