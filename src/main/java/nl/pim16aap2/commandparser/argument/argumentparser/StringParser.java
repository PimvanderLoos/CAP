package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StringParser<T extends String> extends ArgumentParser<T>
{
    @Override
    @SuppressWarnings("unchecked")
    public @NonNull T parseArgument(final @NonNull String value)
    {
        // TODO: Try/catch this stuff.
        return (T) value;
    }

    public static StringParser<String> create()
    {
        return new StringParser<>();
    }
}
