package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.NonNull;

public abstract class ArgumentParser<T>
{
    public abstract @NonNull T parseArgument(final @NonNull String value);
}
