package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Represents an argument parser for String values.
 *
 * @author Pim
 */
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
