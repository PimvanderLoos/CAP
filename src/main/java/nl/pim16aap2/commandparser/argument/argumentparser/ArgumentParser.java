package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.NonNull;
import nl.pim16aap2.commandparser.argument.Argument;

public abstract class ArgumentParser<T>
{
    public abstract @NonNull Argument.ParsedArgument<T> parseArgument(final @NonNull String value);
}
