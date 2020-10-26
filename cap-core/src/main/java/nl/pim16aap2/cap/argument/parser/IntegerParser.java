package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntegerParser extends ArgumentParser<Integer>
{
    @Override
    public @NonNull Integer parseArgument(final @NonNull String value)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Input \"" + value + "\" cannot be parsed into an integer!");
        }
    }

    public static IntegerParser create()
    {
        return new IntegerParser();
    }
}
