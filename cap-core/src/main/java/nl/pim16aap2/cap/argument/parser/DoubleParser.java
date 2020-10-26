package nl.pim16aap2.cap.argument.parser;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Represents an argument parser for double values.
 *
 * @author Pim
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DoubleParser extends ArgumentParser<Double>
{
    @Override
    public @NonNull Double parseArgument(final @NonNull String value)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Input \"" + value + "\" cannot be parsed into a double!");
        }
    }

    public static DoubleParser create()
    {
        return new DoubleParser();
    }
}
