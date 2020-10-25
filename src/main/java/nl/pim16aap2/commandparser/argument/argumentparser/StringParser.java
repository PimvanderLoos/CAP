package nl.pim16aap2.commandparser.argument.argumentparser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StringParser extends ArgumentParser<String>
{
    @Override
    public @NonNull String parseArgument(final @NonNull String value)
    {
        return value;
    }

    public static StringParser create()
    {
        return new StringParser();
    }
}
